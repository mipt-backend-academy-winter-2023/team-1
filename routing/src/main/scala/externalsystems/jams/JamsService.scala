package externalsystems.jams

import externalsystems.jams.JamsProtocol.JamsResponse
import zio.{IO, ZIO}

trait JamsService {
  def getJamsValue(pointId: Int): IO[Serializable, JamsResponse]
}

object JamsService {
  def getJamsValue(
      pointId: Int
  ): ZIO[JamsService, Serializable, JamsResponse] =
    ZIO.serviceWithZIO[JamsService](_.getJamsValue(pointId))
}
