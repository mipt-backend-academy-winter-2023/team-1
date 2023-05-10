package routing

import routing.api.HttpRoutes
import routing.config.ServiceConfig
import zio.http.Server
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object RoutingMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      _ <- ZIO.logInfo("Start RoutingMain")
      _ <- zio.http.Server.serve(HttpRoutes.app)
        .provide(
          Server.live,
          ServiceConfig.live,
        )
    } yield ()
  }
}
