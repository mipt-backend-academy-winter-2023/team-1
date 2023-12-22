package routing.api

import circuitbreaker.MyCircuitBreaker
import jams.Jams
import nl.vroste.rezilience.CircuitBreaker.{CircuitBreakerOpen, WrappedError}
import routing.model.JamId
import routing.model.JsonProtocol._
import routing.model.RoutingRequest
import routing.repository.{
  BuildingRepository,
  CrossroadRepository,
  StreetRepository
}
import routing.utils.{Graph, InvalidAuthorizationToken, JwtUtils}
import io.circe.jawn.decode
import routing.utils.Graph.RouteNotFound
import zio.ZIO
import zio.http._
import zio.http.model.Status.BadRequest
import zio.http.model.{Method, Status}

import scala.collection.concurrent.TrieMap

object HttpRoutes {
  val fallback: TrieMap[Int, JamId] = TrieMap.empty

  val app: HttpApp[
    MyCircuitBreaker with Jams with CrossroadRepository with BuildingRepository with StreetRepository,
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
        jamId <- MyCircuitBreaker
          .run(Jams.getJamId(routingRequest.fromPointId))
          .tap(id => ZIO.succeed(fallback.put(routingRequest.fromPointId, id)))
          .catchAll {
            case CircuitBreakerOpen =>
              val data = fallback.get(routingRequest.fromPointId)
              ZIO.logInfo(s"Get data from fallback $data") *> ZIO.fromOption(
                data
              )
            case WrappedError(error) =>
              ZIO.logError(s"Get error from jams ${error.toString}") *>
                ZIO.fail(error)
          }
        // TODO: преобразовать маршрут в красивый Response
      } yield (route, jamId)).either.map {
        case Right((route, jamId)) =>
          Response
            .text(s"route ${route.toString} with jamId $jamId")
            .setStatus(Status.Ok)
        case Left(InvalidAuthorizationToken(msg)) =>
          Response.text(msg).setStatus(Status.Unauthorized)
        case Left(RouteNotFound(msg)) =>
          Response.text(msg).setStatus(Status.NoContent)
        case Left(_) => Response.status(BadRequest)
      }
    }
}
