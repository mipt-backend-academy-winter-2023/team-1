package auth.repo

import auth.model.User
import auth.utils.PasswordEncoder._
import zio.{ZIO, ZLayer}
import zio.sql.ConnectionPool
import zio.stream.ZStream

final class UserRepositoryImpl(pool: ConnectionPool) extends UserRepository with PostgresTableDescription {

  val driverLayer: ZLayer[Any, Nothing, SqlDriver] =
    ZLayer.make[SqlDriver](SqlDriver.live, ZLayer.succeed(pool))

  override def findAll(): ZStream[Any, Throwable, User] = {
    val selectAll = select(fUsername, fPassword)
      .from(users)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAll is ${renderRead(selectAll)}")
    ) *>
      execute(selectAll.to((User.apply _).tupled))
        .provideSomeLayer(driverLayer)
  }

  override def find(user: User): ZStream[Any, Throwable, User] = {
    val selectUser = select(fUsername, fPassword)
      .from(users)
      .where(fUsername === user.username && fPassword === encode(user.password))

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute find(user) is ${renderRead(selectUser)}")
    ) *> execute(selectUser.to((User.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }

  override def findByUserName(user: User): ZStream[Any, Throwable, User] = {
    val selectUser = select(fUsername, fPassword)
      .from(users)
      .where(fUsername === user.username)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findByUserName(user) is ${renderRead(selectUser)}")
    ) *> execute(selectUser.to((User.apply _).tupled))
      .provideSomeLayer(driverLayer)
  }

  override def add(user: User): ZIO[Any, Throwable, Unit] = {
    val query =
      insertInto(users)(fUsername, fPassword)
        .values((user.username, encode(user.password)))

    ZIO.logInfo(s"Query to insert user is ${renderInsert(query)}") *>
      execute(query)
        .provideSomeLayer(driverLayer)
        .unit
  }
}

object UserRepositoryImpl {
  val live: ZLayer[ConnectionPool, Throwable, UserRepository] =
    ZLayer.fromFunction(new UserRepositoryImpl(_))
}

