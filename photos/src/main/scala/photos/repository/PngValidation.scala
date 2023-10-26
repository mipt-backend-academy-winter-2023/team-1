package photos.repository

import zio.Chunk
import zio.stream.{ZChannel, ZPipeline, ZStream}

object PngValidation {
  // 89 50 4e 47 0d 0a 1a 0a
  final val magic = Chunk(
    0x89,
    0x50,
    0x4e,
    0x47,
    0x0d,
    0x0a,
    0x1a,
    0x0a,
  ).map(_.toByte)

  private def checkHeader(buffer: Chunk[Byte]): ZChannel[Any, Nothing, Chunk[Byte], Any, Throwable, Chunk[Byte], Any] = {
    ZChannel.readOrFail[NotPicture, Chunk[Byte]](NotPicture("is empty"))
      .flatMap { in =>
        val prefix = buffer ++ in
        if (prefix.length < magic.length) {
          checkHeader(prefix)
        } else if (prefix.startsWith(magic)) {
          ZChannel.write(prefix) *> ZChannel.identity[Nothing, Chunk[Byte], Any]
        } else {
          ZChannel.fail(NotPicture("header magic mismatch"))
        }
      }
  }

  final val pipeline = ZPipeline.fromChannel(checkHeader(Chunk.empty[Byte]))
}
