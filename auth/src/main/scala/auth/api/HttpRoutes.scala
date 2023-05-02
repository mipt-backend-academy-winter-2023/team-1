package auth.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "authorization" / "register" =>
        val response =
          for {
            username <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("username")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("not provide username"))

            password <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("password")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("not provide password"))

          } yield Response
            .text(s"Hello $username, your rassword: $password")
            .setStatus(Status.Created)

        response.orElseFail(Response.status(Status.BadRequest))

      case req@Method.POST -> !! / "authorization" / "login" =>
        val response =
          for {
            username <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("username")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("not provide username"))

            password <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("password")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("not provide password"))

          } yield Response
            .text(s"Hello $username, your token: ${Math.abs(username.hashCode + password.hashCode)}")
            .setStatus(Status.Ok)

        response.orElseFail(Response.status(Status.BadRequest))
    }
}
