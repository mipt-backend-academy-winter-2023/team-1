package routing.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case Method.POST -> !! / "routes" =>
        ZIO.succeed(Response.status(Status.NotImplemented))
    }
}
