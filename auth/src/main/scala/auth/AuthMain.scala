package auth

import auth.api.HttpRoutes
import auth.config.{Config, ServiceConfig}
import auth.flyway.FlywayAdapter
import auth.repo.UserRepositoryImpl
import zio.http.Server
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object AuthMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server = for {
      _ <- ZIO.logInfo("Start AuthMain")
      flyway <- ZIO.service[FlywayAdapter.Service]
      _ <- flyway.migration
      server <- zio.http.Server.serve(HttpRoutes.app)
    } yield server

    server.provide(
      Server.live,
      ServiceConfig.live,
      Config.dbLive,
      Config.connectionPoolConfigLive,
      FlywayAdapter.live,
      ConnectionPool.live,
      UserRepositoryImpl.live
    )
  }
}
