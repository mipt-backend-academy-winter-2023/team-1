package photos.repository

import photos.config.S3Config
import zio.{ZIO, ZLayer}
import zio.stream.{ZSink, ZStream}

import java.nio.file.{OpenOption, StandardOpenOption}
import zio.nio.file.Path

trait S3Repository {
  def write(fileStream: ZStream[Any, Throwable, Byte], path: Path): ZIO[Any, Throwable, Any]
  def read(path: Path): ZStream[Any, Throwable, Byte]
}

object S3Repository {
  val live: ZLayer[S3Config, Nothing, S3Repository] =
    ZLayer.fromFunction { config: S3Config => LocalRepository(Path(config.path)) }
}
