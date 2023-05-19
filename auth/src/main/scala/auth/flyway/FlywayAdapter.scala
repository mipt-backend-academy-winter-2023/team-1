package auth.flyway

import auth.config.DbConfig

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.flywaydb.core.api.output.MigrateResult

import zio.{IO, UIO, ZIO, ZLayer}

object FlywayAdapter {
  trait Service extends {
    def migration: IO[FlywayException, MigrateResult]
  }

  val live: ZLayer[DbConfig, Nothing, FlywayAdapter.Service] =
    ZLayer.fromFunction(new FlywayAdapterImpl(_))
}

class FlywayAdapterImpl(dbConfig: DbConfig) extends FlywayAdapter.Service {
  val flyway: UIO[Flyway] = ZIO.succeed(
        Flyway
          .configure()
          .dataSource(dbConfig.url, dbConfig.user, dbConfig.password)
      )
      .map(new Flyway(_))

  override def migration: IO[FlywayException, MigrateResult] =
    flyway.map(_.migrate())
}
