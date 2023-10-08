package routing.repository

import routing.model.Crossroad
import zio.sql.ConnectionPool
import zio.stream.ZStream
import zio.{ZIO, ZLayer}

trait CrossroadRepository {
  def findAllCrossroads: ZStream[Any, Throwable, Crossroad]
}

object CrossroadRepository {
  def findAllCrossroads: ZStream[CrossroadRepository, Throwable, Crossroad] =
    ZStream.serviceWithStream[CrossroadRepository](_.findAllCrossroads)
}

final class CrossroadRepositoryImpl(pool: ConnectionPool)
  extends CrossroadTableDescription
     with CrossroadRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findAllCrossroads: ZStream[Any, Throwable, Crossroad] = {
    val selectAll = select(id, longitude, latitude).from(crossroad)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAllCrossroads is ${renderRead(selectAll)}")
    ) *> execute(selectAll.to((Crossroad.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }
}

object CrossroadRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, CrossroadRepository] =
    ZLayer.fromFunction(new CrossroadRepositoryImpl(_))
}
