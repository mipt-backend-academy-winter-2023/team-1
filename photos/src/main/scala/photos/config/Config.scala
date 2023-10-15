package photos.config

import pureconfig.generic.semiauto.deriveReader
import pureconfig.{ConfigReader, ConfigSource}
import zio.http.ServerConfig
import zio.{ZIO, ZLayer, http}

object Config {
  private val source = ConfigSource.default.at("app")

  val serverLive: ZLayer[Any, Nothing, ServerConfig] =
    zio.http.ServerConfig.live(
      http.ServerConfig.default.port(
        source.loadOrThrow[ConfigImpl].httpServiceConfig.port
      )
    )

  val s3Live: ZLayer[Any, Nothing, S3Config] = {
    ZLayer.fromZIO(ZIO.attempt(source.loadOrThrow[ConfigImpl].s3ServiceConfig).orDie)
  }
}

case class ConfigImpl(httpServiceConfig: HttpServerConfig, s3ServiceConfig: S3Config)

case class HttpServerConfig(
  host: String,
  port: Int
)

case class S3Config(path: String)

object ConfigImpl {
  implicit val configReader: ConfigReader[ConfigImpl] = deriveReader[ConfigImpl]
  implicit val configReaderHttpServerConfig: ConfigReader[HttpServerConfig] = deriveReader[HttpServerConfig]
  implicit val configReaderS3ServiceConfig: ConfigReader[S3Config] = deriveReader[S3Config]
}
