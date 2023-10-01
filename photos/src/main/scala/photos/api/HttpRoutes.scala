package photos.api

import photos.repository.{S3Repository, TooLongRequest}
import photos.utils.{InvalidAuthorizationToken, JwtUtils}
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.nio.file.{Path => FPath}

object HttpRoutes {
  val app: HttpApp[S3Repository, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "photo" / "upload" / id =>
        (for {
          authorizationToken <- ZIO
            .fromOption(req.headers.get("auth_token"))
            .tapError(_ => ZIO.logError("Authorization token not provided"))

          _ <- JwtUtils
            .verifyJwtToken(authorizationToken)
            .tapError(_ => ZIO.logError("Invalid authorization token"))

          s3Repository <- ZIO.service[S3Repository]

          result <- s3Repository.write(req.body.asStream, FPath(id))
        } yield result).either.map {
          case Right(_) => Response.status(Status.Ok)
          case Left(InvalidAuthorizationToken(msg)) => Response.text(msg).setStatus(Status.Unauthorized)
          case Left(TooLongRequest) => Response.text("Too big request").setStatus(Status.RequestEntityTooLarge)
          case Left(_) => Response.status(Status.BadRequest)
        }
      case req @ Method.GET -> !! / "photo" / "get" / id =>
        (for {
          s3Repository <- ZIO.service[S3Repository]

          result <- s3Repository.read(FPath(id))
        } yield result).either.map {
          case Right(_) => Response.status(Status.Ok)
          case Left(_) => Response.status(Status.NotFound)
        }
    }
}
