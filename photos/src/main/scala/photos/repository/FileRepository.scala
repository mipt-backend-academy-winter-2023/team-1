package photos.repository

import photos.config.S3Config
import photos.repository.PhotoRepository.Photo
import zio.{Chunk, IO, ZIO, ZLayer}
import zio.nio.file.Path
import zio.stream.{ZSink, ZStream}

import java.nio.file.{OpenOption, StandardOpenOption}

class FileRepository(val localPath: Path) extends PhotoRepository {
  override def write(path: Path, fileStream: ZStream[Any, Throwable, Byte]): ZIO[Any, PhotoRepoError, Unit] = {
    import photos.repository.FileRepository.writeFile
    writeFile(localPath / path, fileStream).map(_ => ())
  }

  override def read(path: Path): ZStream[Any, PhotoRepoError, Byte] = {
    import photos.repository.FileRepository.readFile

    readFile(localPath / path)
  }
}

object FileRepository {
  def apply(localPath: Path): PhotoRepository = new FileRepository(localPath)


  val live: ZLayer[S3Config, Throwable, PhotoRepository] =
    ZLayer.fromFunction { config: S3Config => FileRepository(Path(config.path)) }

  private def writeFile(path: Path, fileStream: ZStream[Any, Throwable, Byte]): ZIO[Any, PhotoRepoError, Long] = {
    val options: Set[OpenOption] = Set(
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE,
      StandardOpenOption.SYNC,
    )
    val pictureStream = fileStream.take(PhotoRepository.maxByteSize)
    val pngStream: ZStream[Any, PhotoRepoError, Byte] = PngValidation.pipeline(pictureStream).mapError(RuntimeError.apply)
    val fileSink = ZSink.fromFile(path.toFile, options = options)
    (pngStream >>> fileSink).mapError(RuntimeError.apply)
  }

  private def readFile(path: Path): ZStream[Any, PhotoRepoError, Byte] = {
    ZStream.fromFile(path.toFile).mapError(NotFound.apply)
  }
}
