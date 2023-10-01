package photos

import photos.api.HttpRoutes
import photos.config.Config
import photos.repository.S3Repository
import zio.http.Server
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object PhotosMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server = for {
      _ <- ZIO.logInfo("Start RoutingMain")
      server <- zio.http.Server.serve(HttpRoutes.app)
    } yield server

    server.provide(
      Config.serverLive,
      Server.live,
      Config.s3Live,
      S3Repository.live,
    )
  }
}
