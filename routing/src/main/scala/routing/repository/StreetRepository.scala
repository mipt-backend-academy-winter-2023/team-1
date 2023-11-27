package routing.repository

import routing.model.Street
import zio.sql.ConnectionPool
import zio.stream.ZStream
import zio.{ZIO, ZLayer}

trait StreetRepository {
  def findAllStreets: ZStream[Any, Throwable, Street]
}

object StreetRepository {
  def findAllStreets: ZStream[StreetRepository, Throwable, Street] =
    ZStream.serviceWithStream[StreetRepository](_.findAllStreets)
}

final class StreetRepositoryImpl(pool: ConnectionPool)
    extends StreetTableDescription
    with StreetRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findAllStreets: ZStream[Any, Throwable, Street] = {
    val selectAll = select(fromId, toId, name).from(street)

    ZStream.fromZIO(
      ZIO.logInfo(
        s"Query to execute findAllStreets is ${renderRead(selectAll)}"
      )
    ) *> execute(selectAll.to((Street.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }
}

object StreetRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, StreetRepository] =
    ZLayer.fromFunction(new StreetRepositoryImpl(_))
}
