package auth.api

import auth.model.User
import auth.repo.UserRepository
import auth.utils.JwtUtils._
import zio.ZIO
import zio.http._
import zio.http.model.Method
import zio.http.model.Status._
import io.circe.jawn.decode
import zio.http.model.Status.BadRequest

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "authorization" / "register" =>
        (for {
          bodyStr <- req.body.asString
          user <- ZIO.fromEither(decode[User](bodyStr)).tapError(e => ZIO.logError(e.getMessage))
          found <- UserRepository.findByUserName(user).runCollect.map(_.toArray)
        } yield (user, found)).either.map {
          case Right((newUser, users)) => users match {
            case Array() =>
              UserRepository.add(newUser)
              ZIO.logInfo(s"Registered user: $newUser")
              Response.status(Created)
            case _ => Response.status(Conflict)
          }
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
              Response.text(s"{\"token\": \"${generateToken(oldUser.username)}\"}").setStatus(Ok)
          }
          case Left(_) => Response.status(BadRequest)
        }
    }
}
