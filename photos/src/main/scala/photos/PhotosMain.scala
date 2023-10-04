package photos

import photos.api.HttpRoutes
import photos.config.Config
import zio.http.Server
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object PhotosMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server = for {
      _ <- ZIO.logInfo("Start PhotosMain")
      server <- zio.http.Server.serve(HttpRoutes.app)
    } yield server

    server.provide(
      Server.live,
      Config.live
    )
  }
}
