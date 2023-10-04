package photos.config

import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader
import zio.http.ServerConfig
import zio.ZLayer

case class Config(host: String, port:  Int)

object Config {
  private val source = ConfigSource.default.at("app").at("http-service-config")
  private val serviceConfig: Config = source.loadOrThrow[Config]

  val live: ZLayer[Any, Nothing, ServerConfig] = ServerConfig.live {
    ServerConfig.default
      .binding(serviceConfig.host, serviceConfig.port)
      .objectAggregator(10 * 1024 * 1024)
  }
}
