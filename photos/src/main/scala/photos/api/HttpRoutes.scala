package photos.api

import zio.ZIO
import zio.http._
import zio.http.model.Method
import zio.http.model.Status.Ok

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "photos" / "upload" =>
        ZIO.succeed(Response.status(Ok))

      case req @ Method.GET -> !! / "photos"/ "download" =>
        ZIO.succeed(Response.status(Ok))
    }
}
