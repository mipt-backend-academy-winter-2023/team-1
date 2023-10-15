package photos.repository

import zio.{IO, URLayer, ZIO, ZLayer}
import zio.stream.{ZSink, ZStream}
import zio.nio.file.Path

sealed trait S3Error
case class TooBig(length: Int) extends S3Error
case class RuntimeError(ex: Throwable) extends S3Error
case object NotPicture extends S3Error {
  def apply(): S3Error = NotPicture
}
case object NotFound extends S3Error {
  def apply(): S3Error = NotFound
}

trait S3Repository {
  def write(fileStream: ZStream[Any, Throwable, Byte], path: Path): IO[S3Error, Unit]
  def read(path: Path): ZStream[Any, S3Error, Byte]
}

object S3Repository {
}
