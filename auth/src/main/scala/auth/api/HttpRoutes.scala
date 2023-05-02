package auth.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case Method.POST -> !! / "authorization" / "login" =>
        ZIO.succeed(Response.status(Status.NotImplemented))
      case Method.POST -> !! / "authorization" / "register" =>
        ZIO.succeed(Response.status(Status.NotImplemented))
    }
}
