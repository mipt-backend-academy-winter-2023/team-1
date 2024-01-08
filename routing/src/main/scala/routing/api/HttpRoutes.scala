package routing.api

import circuitbreaker.ZCircuitBreaker
import externalsystems.jams.JamsProtocol.JamsResponse
import externalsystems.jams.JamsService
import routing.model.JsonProtocol._
import routing.model.RoutingRequest
import routing.repository.{
  BuildingRepository,
  CrossroadRepository,
  StreetRepository
}
import routing.utils.{Graph, InvalidAuthorizationToken, JwtUtils}
import io.circe.jawn.decode
import nl.vroste.rezilience.CircuitBreaker.CircuitBreakerException
import routing.utils.Graph.RouteNotFound
import zio.{Task, ZIO}
import zio.http._
import zio.http.model.Status.BadRequest
import zio.http.model.{Method, Status}

import scala.collection.concurrent.TrieMap

object HttpRoutes {
  val fallback: TrieMap[Int, JamsResponse] = TrieMap.empty

  val app: HttpApp[
    CrossroadRepository with BuildingRepository with StreetRepository with JamsService with ZCircuitBreaker,
    Response
  ] =
    Http.collectZIO[Request] { case req @ Method.POST -> !! / "routes" =>
      (for {
        authorizationToken <- ZIO
          .fromOption(req.headers.get("auth_token"))
          .tapError(_ => ZIO.logError("Authorization token not provided"))

        _ <- JwtUtils
          .verifyJwtToken(authorizationToken)
          .tapError(_ => ZIO.logError("Invalid authorization token"))

        bodyStr <- req.body.asString
        routingRequest <- ZIO
          .fromEither(decode[RoutingRequest](bodyStr))
          .tapError(_ => ZIO.logError("Points' ids not provided"))

        _ <- Graph
          .findGeoPointByIdSafe(routingRequest.fromPointId)
          .tapError(_ => ZIO.logError("Invalid point id"))
        _ <- Graph
          .findGeoPointByIdSafe(routingRequest.toPointId)
          .tapError(_ => ZIO.logError("Invalid point id"))

        route <- Graph.searchForShortestRoute(routingRequest)

        jamValue <- ZCircuitBreaker
          .run(JamsService.getJamsValue(routingRequest.fromPointId))
          .tap(jam =>
            ZIO.succeed(fallback.put(routingRequest.fromPointId, jam))
          )
          .catchAll(error =>
            fallback
              .get(routingRequest.fromPointId)
              .fold[Task[JamsResponse]](
                ZIO.logError(s"Jams value not found: ${error.toString}") *>
                  ZIO.fail(error.toException)
              )(value =>
                ZIO.logInfo(s"Got jams value from fallback: $value") *>
                  ZIO.succeed(value)
              )
          )
      } yield (route, jamValue)).either.map {
        case Right((route, jamValue)) =>
          Response
            .text(s"route: ${route.mkString(" ----> ")}\njam value: $jamValue")
            .setStatus(Status.Ok)
        case Left(CircuitBreakerException(error)) =>
          Response
            .text(s"Jams service is not available: ${error.toString}")
            .setStatus(Status.InternalServerError)
        case Left(InvalidAuthorizationToken(msg)) =>
          Response.text(msg).setStatus(Status.Unauthorized)
        case Left(RouteNotFound(msg)) =>
          Response.text(msg).setStatus(Status.NoContent)
        case Left(_) => Response.status(BadRequest)
      }
    }
}
