package photos

import photos.api.HttpRoutes
import photos.config.Config
import photos.repository.{FileRepository, PhotoRepository}
import zio.http.Server
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object PhotosMain extends ZIOAppDefault {
  def kafkaLayer =
    ZLayer.scoped(
      Producer.make(
        settings = ProducerSettings(List("kafka-1:9092"))
      )
    )
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val server = for {
      _ <- ZIO.logInfo("Start PhotosMain")
      server <- zio.http.Server.serve(HttpRoutes.app)
    } yield server

    server.provide(
      Config.serverLive,
      Server.live,
      Config.s3Live,
      FileRepository.live,
      kafkaLayer
    )
  }
}
