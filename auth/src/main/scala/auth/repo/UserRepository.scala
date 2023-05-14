package auth.repo

import auth.model.User
import zio.stream.ZStream
import zio.{Task, ZIO}

trait UserRepository {
  def find(user: User): ZStream[Any, Throwable, User]
  def findByUserName(user: User): ZStream[Any, Throwable, User]
  def add(user: User): Task[Unit]
}

object UserRepository {
  def find(user: User): ZStream[UserRepository, Throwable, User] =
    ZStream.serviceWithStream[UserRepository](_.find(user))

  def findByUserName(user: User): ZStream[UserRepository, Throwable, User] =
    ZStream.serviceWithStream[UserRepository](_.findByUserName(user))

  def add(user: User): ZIO[UserRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[UserRepository](_.add(user))
}
