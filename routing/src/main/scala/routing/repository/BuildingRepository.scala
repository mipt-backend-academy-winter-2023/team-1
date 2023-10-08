package routing.repository

import routing.model.Building
import zio.sql.ConnectionPool
import zio.stream.ZStream
import zio.{ZIO, ZLayer}

trait BuildingRepository {
  def findAllBuildings: ZStream[Any, Throwable, Building]
}

object BuildingRepository {
  def findAllBuildings: ZStream[BuildingRepository, Throwable, Building] =
    ZStream.serviceWithStream[BuildingRepository](_.findAllBuildings)
}

final class BuildingRepositoryImpl(pool: ConnectionPool)
  extends BuildingTableDescription
     with BuildingRepository {
  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findAllBuildings: ZStream[Any, Throwable, Building] = {
    val selectAll = select(id, longitude, latitude, name).from(building)

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
