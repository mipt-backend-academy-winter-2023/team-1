package routing

import circuitbreaker.MyCircuitBreaker
import circuitbreaker.MyCircuitBreakerImpl
import jams.JamsImpl
import routing.api.HttpRoutes
import routing.config.Config
import routing.flyway.FlywayAdapter
import routing.repository.{
  BuildingRepositoryImpl,
  CrossroadRepositoryImpl,
  StreetRepositoryImpl
}
import routing.utils.Graph
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.http.Server
import zio.sql.ConnectionPool
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object RoutingMain extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server = for {
      _ <- ZIO.logInfo("Start RoutingMain")
      flyway <- ZIO.service[FlywayAdapter.Service]
      _ <- flyway.migration
      _ <- Graph.reload.tapError(_ => ZIO.logError("Invalid graph"))
      server <- zio.http.Server.serve(HttpRoutes.app)
    } yield server

    server.provide(
      Server.live,
      Config.serverLive,
      HttpClientZioBackend.layer(),
      Config.dbLive,
      FlywayAdapter.live,
      Config.connectionPoolConfigLive,
      ConnectionPool.live,
      StreetRepositoryImpl.live,
      BuildingRepositoryImpl.live,
      CrossroadRepositoryImpl.live,
      MyCircuitBreakerImpl.live,
      JamsImpl.live,
      Scope.default
    )
  }
}
