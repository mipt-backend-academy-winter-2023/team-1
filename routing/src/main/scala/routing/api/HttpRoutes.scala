package routing.api

import routing.model.JsonProtocol._
import routing.model.RoutingRequest
import routing.repository.{BuildingRepository, CrossroadRepository, StreetRepository}
import routing.utils.{Graph, InvalidAuthorizationToken, JwtUtils}
import io.circe.jawn.decode
import routing.utils.Graph.RouteNotFound
import zio.ZIO
import zio.http._
import zio.http.model.Status.BadRequest
import zio.http.model.{Method, Status}

object HttpRoutes {
  val app: HttpApp[CrossroadRepository with BuildingRepository with StreetRepository, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "routes" =>
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

          route <- Graph.searchForShortestRoute(routingRequest)
          // TODO: преобразовать маршрут в красивый Response
        } yield route).either.map {
          case Right(route) => Response.text(route.toString).setStatus(Status.Ok)
          case Left(InvalidAuthorizationToken(msg)) => Response.text(msg).setStatus(Status.Unauthorized)
          case Left(RouteNotFound(msg)) => Response.text(msg).setStatus(Status.NoContent)
          case Left(_) => Response.status(BadRequest)
        }
    }
}
