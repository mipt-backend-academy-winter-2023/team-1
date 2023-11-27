package photos.repository

import photos.repository.PhotoRepository.Photo
import zio.{Chunk, IO}
import zio.stream.{ZSink, ZStream}
import zio.nio.file.Path

sealed trait PhotoRepoError extends Throwable
case class TooBig(length: Int) extends PhotoRepoError
case class RuntimeError(ex: Throwable) extends PhotoRepoError
case class NotPicture(context: String) extends PhotoRepoError
case object NotFound extends PhotoRepoError {
  def apply(unused: Any): PhotoRepoError = NotFound
}

trait PhotoRepository {
  def write(
      path: Path,
      fileStream: ZStream[Any, Throwable, Byte]
  ): IO[PhotoRepoError, Unit]
  def read(path: Path): ZStream[Any, PhotoRepoError, Byte]
}

object PhotoRepository {
  type Photo = Chunk[Byte]
  val maxByteSize = 10 * 1024 * 1024
}
