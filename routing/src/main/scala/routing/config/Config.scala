package routing.config

import pureconfig.generic.semiauto.deriveReader
import pureconfig.{ConfigReader, ConfigSource}
import zio.http.ServerConfig
import zio.sql.ConnectionPoolConfig
import zio.{ULayer, ZIO, ZLayer, http}

import java.util.Properties

object Config {
  private val source = ConfigSource.default.at("app")

  val dbLive: ULayer[DbConfig] = {
    import ConfigImpl._
    ZLayer.fromZIO(ZIO.attempt(source.loadOrThrow[ConfigImpl].dbConfig).orDie)
  }

  val serverLive: ZLayer[Any, Nothing, ServerConfig] =
    zio.http.ServerConfig.live(
      http.ServerConfig.default.port(
        source.loadOrThrow[ConfigImpl].httpServiceConfig.port
      )
    )

  val connectionPoolConfigLive: ZLayer[DbConfig, Throwable, ConnectionPoolConfig] =
    ZLayer(
      for {
        serverConfig <- ZIO.service[DbConfig]
      } yield ConnectionPoolConfig(
        serverConfig.url,
        connProperties(serverConfig.user, serverConfig.password)
      )
    )

  private def connProperties(user: String, password: String): Properties = {
    val props = new Properties
    props.setProperty("user", user)
    props.setProperty("password", password)
    props
  }
}

case class ConfigImpl(
  dbConfig: DbConfig,
  httpServiceConfig: HttpServerConfig
)

case class DbConfig(
  url: String,
  user: String,
  password: String
)

case class HttpServerConfig(
  host: String,
  port: Int
)

object ConfigImpl {
  implicit val configReader: ConfigReader[ConfigImpl] = deriveReader[ConfigImpl]
  implicit val configReaderHttpServerConfig: ConfigReader[HttpServerConfig] = deriveReader[HttpServerConfig]
  implicit val configReaderDbConfig: ConfigReader[DbConfig] = deriveReader[DbConfig]
}
