package helper.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}

object HelpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "hello" => ZIO.succeed(Response.text("Hello"))
      case req @ Method.POST -> !! / "greeting" / "by" =>
        val response =
          for {
            name <- ZIO
              .fromOption(
                req.url.queryParams
                  .get("name")
                  .flatMap(_.headOption)
              )
              .tapError(_ => ZIO.logError("not provide id"))
          } yield Response.text(s"Hello $name")

        response.orElseFail(Response.status(Status.BadRequest))
    }
}
