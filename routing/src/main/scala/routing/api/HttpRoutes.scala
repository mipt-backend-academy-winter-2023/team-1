package routing.api

import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "routes" => {
        val response =
          for {
            ids <- ZIO.fromOption(
              req
                .url
                .queryParams
                .get("ids")
                .flatMap(_.headOption)
            ).tapError(_ => ZIO.logError("ids not set up"))
          } yield Response.text("Done")

        ZIO.succeed(Response.status(Status.NotImplemented))
      }
    }
}
