package photos.repository

import photos.config.S3Config
import zio.{ZIO, ZLayer}
import zio.nio.file.Path
import zio.stream.{ZSink, ZStream}

import java.nio.file.{OpenOption, StandardOpenOption}

class FileRepository(val localPath: Path) extends S3Repository {
  override def write(fileStream: ZStream[Any, Throwable, Byte], path: Path): ZIO[Any, S3Error, Unit] = {
    import photos.repository.FileRepository.writeFile
    writeFile(fileStream, localPath / path)
  }

  override def read(path: Path): ZStream[Any, Throwable, Byte] = {
    import photos.repository.FileRepository.readFile

    readFile(localPath / path)
  }
}

object FileRepository {
  def apply(localPath: Path): S3Repository = new FileRepository(localPath)


  val live: ZLayer[S3Config, Throwable, S3Repository] =
    ZLayer.fromFunction { config: S3Config => FileRepository(Path(config.path)) }

  private def writeFile(fileStream: ZStream[Any, Throwable, Byte], path: Path): ZIO[Any, S3Error, Unit] = {
    val options: Set[OpenOption] = Set(
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE,
      StandardOpenOption.SYNC,
    )
    val fileSink = ZSink.fromFile(path.toFile, options = options)

    val result = for {
      _ <- (fileStream >>> fileSink).mapError(ex => RuntimeError(ex))
    } yield ()

    result
  }

  private def readFile(path: Path): ZStream[Any, Throwable, Byte] = {
    ZStream.fromFile(path.toFile)
  }
}
