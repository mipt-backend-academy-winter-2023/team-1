package routing.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "routes" =>
        val response =
          for {
            authToken <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("auth_token")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("not provide auth_token"))

            idArray <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("id_array")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("not provide id_array"))

          } yield Response
            .text(s"Provided token: $authToken\nGot: $idArray")
            .setStatus(Status.Ok)
        response.orElseFail(Response.status(Status.BadRequest))
    }
}
