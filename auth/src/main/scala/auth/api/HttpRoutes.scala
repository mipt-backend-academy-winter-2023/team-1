package auth.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "authorization" / "login" => {
        val response =
          for {
            username <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("username")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("username not set up"))
          } yield Response.text("Logged in successfully")

        ZIO.succeed(Response.status(Status.NotImplemented))
      }
      case req@Method.POST -> !! / "authorization" / "register" => {
        val response =
          for {
            username <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("username")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("username not set up"))
          } yield Response.text("Account created successfully")

        ZIO.succeed(Response.status(Status.NotImplemented))
      }
    }
}
