package routing.repository

import routing.model.Building
import zio.sql.ConnectionPool
import zio.stream.ZStream
import zio.{ZIO, ZLayer}

trait BuildingRepository {
  def findAllStreets: ZStream[Any, Throwable, Building]
}

object BuildingRepository {
  def findAllStreets: ZStream[BuildingRepository, Throwable, Building] =
    ZStream.serviceWithStream[BuildingRepository](_.findAllStreets)
}

final class BuildingRepositoryImpl(pool: ConnectionPool)
  extends PostgresTableDescription
     with BuildingRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findAllStreets: ZStream[Any, Throwable, Building] = {
    val selectAll = select().from(building)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAllStreets is ${renderRead(selectAll)}")
    ) *> execute(selectAll.to((Building.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }
}

object BuildingRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, BuildingRepository] =
    ZLayer.fromFunction(new BuildingRepositoryImpl(_))
}
