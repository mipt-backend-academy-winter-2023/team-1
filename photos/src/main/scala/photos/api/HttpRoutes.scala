package photos.api

import photos.{Api, Auth, Repo}
import photos.repository.{NotPicture, PhotoRepoError, PhotoRepository, TooBig}
import photos.utils.{AuthError, JwtError, JwtUtils, PhotosValidation}
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.nio.file.{Path => FPath}

import scala.util.Try

sealed trait ApiError

case object NoContentLength extends ApiError {
  def apply: ApiError = NoContentLength
}

object HttpRoutes {
  val app: HttpApp[PhotoRepository, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "photo" / "upload" / id =>
        (for {
          authorizationToken <- ZIO
            .fromOption(req.headers.get("auth_token"))
            .tapError(_ => ZIO.logError("Authorization token not provided"))

          _ <- JwtUtils
            .verifyJwtToken(authorizationToken)
            .tapError(_ => ZIO.logError("Invalid authorization token"))

          contentLength <- ZIO.fromOption(req.headers.get("Content-Length"))
            .mapError(_ => NoContentLength)
            .tapError(_ => ZIO.logError("No Content-Length option"))
            .map(_.toInt)

          _ <- ZIO.unit.filterOrElse(_ => contentLength > 0)(ZIO.fail(TooBig(0)))

          _ <- ZIO.unit
            .filterOrElse(_ => contentLength < PhotoRepository.maxByteSize)(ZIO.fail(TooBig(contentLength)))

          photoR <- ZIO.service[PhotoRepository]

          result <- photoR.write(FPath(id), req.body.asStream.via(PhotosValidation.pipeline))
        } yield result)
          .map {
            _ => Response.status(Status.Ok)
          }
          .mapError {
            case Auth(error: AuthError) => error match {
              case JwtError(msg) => Response.text(msg).setStatus(Status.Unauthorized)
            }
            case Api(error: ApiError) => error match {
              case NoContentLength => Response.text("No Content-Length").setStatus(Status.BadRequest)
            }
            case Repo(error: PhotoRepoError) => error match {
              case TooBig(length) => Response.text(f"Too big request: $length").setStatus(Status.RequestEntityTooLarge)
              case NotPicture => Response.text("File is not a picture").setStatus(Status.BadRequest)
            }
            case error: PhotosValidation.Error => Response.text(error.getMessage).setStatus(Status.NotAcceptable)
          }
      case req @ Method.GET -> !! / "photo" / "get" / id =>
        (for {
          photoR <- ZIO.service[PhotoRepository]
          result <- ZIO.fromTry(Try(Body.fromStream(photoR.read(FPath(id)))))
        } yield result).either.map {
          case Right(body) => Response(status = Status.Ok, body = body)
          case Left(_) => Response.status(Status.NotFound)
        }
      case _ => ZIO.succeed(Response.status(Status.NotImplemented))
    }
}
