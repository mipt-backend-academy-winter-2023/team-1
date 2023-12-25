package compressor

import zio._
import zio.kafka.consumer._
import zio.kafka.producer.ProducerSettings
import zio.kafka.serde._
import zio.stream.ZStream

object CompressorMain extends ZIOAppDefault {
  val consumer: ZStream[Consumer, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("photos"), Serde.string, Serde.string)
//      .map(photo => compressPhoto(photo))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain

  override def run = consumer.runDrain.provide(
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(List("kafka-1:9092"))
      )
    )
  )
}
