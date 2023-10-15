package photos.api

import photos.{Auth, PhotoError, S3}
import photos.repository.{NotPicture, S3Error, S3Repository, TooBig, TooLongRequest}
import photos.utils.{AuthError, JwtError, JwtUtils}
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
        } yield result)
          .map {
            _ => Response.status(Status.Ok)
          }
          .mapError {
            case Auth(error: AuthError) => error match {
              case JwtError(msg) => Response.text(msg).setStatus(Status.Unauthorized)
            }
            case S3(error: S3Error) => error match {
              case TooBig(length) => Response.text(f"Too big request: $length").setStatus(Status.RequestEntityTooLarge)
              case NotPicture => Response.text("File is not a picture").setStatus(Status.BadRequest)
            }
          }
//      case req @ Method.GET -> !! / "photo" / "get" / id =>
//        (for {
//          s3Repository <- ZIO.service[S3Repository]
//
//          result <- s3Repository.read(FPath(id))
//        } yield result).either.map {
//          case Right(_) => Response.status(Status.Ok)
//          case Left(_) => Response.status(Status.NotFound)
//        }
      case _ => ZIO.succeed(Response.status(Status.NotImplemented))
    }
}
