import zio.test.{ZIOSpecDefault, suite, test, assertTrue}
import zio.{ZIO, ZLayer}
import zio.http._
import zio.http.model.Status
import zio.stream.ZStream

import auth.model.User
import auth.api.HttpRoutes
import auth.repo.UserRepository

import scala.collection.mutable

object ExampleSpec extends ZIOSpecDefault {
  final class TestUserRepository(allUsers: mutable.HashMap[String, User])
      extends UserRepository {
    override def findAll(): ZStream[Any, Throwable, User] = {
      ZStream.fromIterable(allUsers.values)
    }

    override def find(user: User): ZStream[Any, Throwable, User] = {
      if (
        allUsers.contains(user.username) && allUsers(
          user.username
        ).password == user.password
      ) {
        return ZStream.fromZIO(ZIO.succeed(allUsers(user.username)))
      }
      ZStream.empty
    }

    override def findByUserName(user: User): ZStream[Any, Throwable, User] = {
      if (allUsers.contains(user.username)) {
        return ZStream.fromZIO(ZIO.succeed(allUsers(user.username)))
      }
      ZStream.empty
    }
    override def add(user: User): ZIO[Any, Throwable, Unit] = {
      if (allUsers.contains(user.username)) {
        return ZIO.fail(
          new Exception(s"User with name ${user.username} is already exists")
        )
      }
      allUsers += user.username -> user
      ZIO.succeed()
    }
  }
  def getTestUserRespository() = {
    ZLayer.succeed(new TestUserRepository(mutable.HashMap.empty))
  }

  def makeHTTPRequest(
      HTTPMethod: String,
      user: User
  ): ZIO[UserRepository, Option[Response], Response] =
    HttpRoutes.app.runZIO(
      Request.post(
        Body.fromString(
          s"""{"username":"${user.username}","password": "${user.password}"}"""
        ),
        URL(!! / "authorization" / HTTPMethod)
      )
    )

  def register(user: User) = makeHTTPRequest("register", user)
  def login(user: User) = makeHTTPRequest("login", user)

  override def spec = suite("Auth tests")(
    test("Test registration flow") {
      val user1 = new User("Bob", "Bob_password")
      val user2 = new User("Tom", "Tom_password")
      val user3 = new User("Bob", "Bob_password2")
      val user4 = new User("Sam", "Bob_password")
      (for {
        resp1 <- register(user1)
        resp2 <- register(user2)
        resp3 <- register(user3)
        resp4 <- register(user4)
      } yield {
        assertTrue(resp1.status == Status.Created) // correct
        assertTrue(resp2.status == Status.Created) // correct
        assertTrue(resp3.status == Status.BadRequest) // incorrect, same name
        assertTrue(
          resp4.status == Status.Created
        ) // correct, same password but new name
      }).provideLayer(getTestUserRespository())
    },
    test("Test login flow") {
      val user1 = new User("Bob", "Bob_password")
      val user1_bad = new User("Bob", "Bob_bad_password")
      val user2 = new User("Tom", "Tom_password")
      val user3 = new User("Sam", "Sam_password")
      (for {
        resp1 <- login(user1)
        resp2 <- register(user1)
        resp3 <- login(user1)
        resp4 <- login(user1)
        resp5 <- login(user1_bad)

        resp6 <- login(user2)
        resp7 <- register(user2)

        resp8 <- login(user3)
        resp9 <- login(user2)
        resp10 <- login(user1)
      } yield {
        assertTrue(resp1.status == Status.Unauthorized) // unknown user
        assertTrue(resp2.status == Status.Created) // registration
        assertTrue(resp3.status == Status.Ok) // correct
        assertTrue(
          resp4.status == Status.Ok
        ) // correct, can login several times
        assertTrue(resp5.status == Status.Unauthorized) // bad password for Bob

        assertTrue(resp6.status == Status.Unauthorized) // unknown user
        assertTrue(resp7.status == Status.Created) // registration

        // users are independent
        assertTrue(
          resp8.status == Status.Unauthorized
        ) // incorrect, another user made registration
        assertTrue(resp9.status == Status.Ok) // correct
        assertTrue(resp10.status == Status.Ok) // correct, still can login
      }).provideLayer(getTestUserRespository())
    }
  )
}
