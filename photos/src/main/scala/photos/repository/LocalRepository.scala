package photos.repository

import zio.ZIO
import zio.nio.file.Path
import zio.stream.{ZSink, ZStream}

import java.nio.file.{OpenOption, StandardOpenOption}

case object TooLongRequest extends Exception {
}

class LocalRepository(val localPath: Path) extends S3Repository {
  override def write(fileStream: ZStream[Any, Throwable, Byte], path: Path): ZIO[Any, Throwable, Any] = {
    import photos.repository.LocalRepository.writeFile

    writeFile(fileStream, localPath / path)
  }

  override def read(path: Path): ZStream[Any, Throwable, Byte] = {
    import photos.repository.LocalRepository.readFile

    readFile(localPath / path)
  }
}

object LocalRepository {
  def apply(localPath: Path): S3Repository = new LocalRepository(localPath)

  private def writeFile(fileStream: ZStream[Any, Throwable, Byte], path: Path): ZIO[Any, Throwable, Long] = {
    val options: Set[OpenOption] = Set(
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE,
      StandardOpenOption.SYNC,
    )
    val fileSink = ZSink.fromFile(path.toFile, options = options)
    fileStream >>> fileSink
  }

  private def readFile(path: Path): ZStream[Any, Throwable, Byte] = {
    ZStream.fromFile(path.toFile)
  }
}
