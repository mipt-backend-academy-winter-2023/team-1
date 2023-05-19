package auth.api

import auth.model.JsonProtocol._
import auth.model.User
import auth.repo.UserRepository
import auth.utils.JwtUtils._
import io.circe.jawn.decode
import zio.ZIO
import zio.http._
import zio.http.model.Method
import zio.http.model.Status._

import java.sql.SQLException

object HttpRoutes {
  val app: HttpApp[UserRepository, Response] =
    Http.collectZIO[Request] {

      case req@Method.POST -> !! / "authorization" / "register" =>
        (for {
          bodyStr <- req.body.asString
          user <- ZIO.fromEither(decode[User](bodyStr)).tapError(e => ZIO.logError(e.getMessage))
          _ <- UserRepository.add(user)
          _ <- ZIO.logInfo(s"Registered user: $user")
        } yield ()).either.map {
          case Right(_) => Response.status(Created)
          case Left(_: SQLException) => Response.status(Conflict)
          case Left(_) => Response.status(BadRequest)
        }

      case req@Method.POST -> !! / "authorization" / "login" =>
        (for {
          bodyStr <- req.body.asString
          user <- ZIO.fromEither(decode[User](bodyStr)).tapError(e => ZIO.logError(e.getMessage))
          found <- UserRepository.find(user).runCollect.map(_.toArray)
        } yield (user, found)).either.map {
          case Right((oldUser, users)) => users match {
            case Array() => Response.status(Unauthorized)
            case _ =>
              ZIO.logInfo(s"Authorized user: $oldUser")
              Response.text(s"{\n\"token\": \"${generateToken(oldUser.username)}\"\n}").setStatus(Ok)
          }
          case Left(_) => Response.status(BadRequest)
        }
    }
}
