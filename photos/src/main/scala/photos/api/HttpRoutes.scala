package photos.api

import photos.utils.{InvalidAuthorizationToken, JwtUtils}
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.stream.ZSink

import java.io.{File, IOException}
import java.nio.file.{Files, Paths}

case class ConflictError(msg: String) extends Exception(msg)
case class FileNotFoundError(msg: String) extends Exception(msg)

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "photos" / "upload" / id =>
        (for {
          authorizationToken <- ZIO
            .fromOption(req.headers.get("auth_token"))
            .tapError(_ => ZIO.logError("Authorization token not provided"))

          _ <- JwtUtils
            .verifyJwtToken(authorizationToken)
            .tapError(_ => ZIO.logError("Invalid authorization token"))

          outputPath = Paths.get(".", "src", s"${id}_image.png")

          _ = if (Files.notExists(outputPath.getParent))
            Files.createDirectories(outputPath.getParent)

          path <- ZIO.from(Files.createFile(outputPath))
            .tapError(_ => ZIO.logInfo("Unable to create file"))

          _ <- req.body.asStream
            .run(ZSink.fromPath(path))
            .catchAll(_ => ZIO.fail(ConflictError("Files conflict")))
        } yield ()).either.map {
          case Right(_) => Response.status(Status.Ok)
          case Left(InvalidAuthorizationToken(msg)) => Response.text(msg).setStatus(Status.Unauthorized)
          case Left(ConflictError(msg)) => Response.text(msg).setStatus(Status.Conflict)
          case Left(IOException) => Response.status(Status.NoContent)
          case Left(_) => Response.status(Status.BadRequest)
        }

      case req @ Method.GET -> !! / "photos"/ "download" / id =>
        (for {
          authorizationToken <- ZIO
            .fromOption(req.headers.get("auth_token"))
            .tapError(_ => ZIO.logError("Authorization token not provided"))

          _ <- JwtUtils
            .verifyJwtToken(authorizationToken)
            .tapError(_ => ZIO.logError("Invalid authorization token"))

          outputPath = Paths.get(".", "src", s"${id}_image.png")

          resp <-
            if (Files.notExists(outputPath))
              ZIO.fail(FileNotFoundError("No such file"))
            else
              ZIO.succeed(
                Response(
                  status = Status.Ok,
                  body = Body.fromFile(new File(outputPath.toAbsolutePath.toString))
                )
              )
        } yield resp).either.map {
          case Right(resp) => resp
          case Left(InvalidAuthorizationToken(msg)) => Response.text(msg).setStatus(Status.Unauthorized)
          case Left(FileNotFoundError(msg)) => Response.text(msg).setStatus(Status.NotFound)
          case Left(_) => Response.status(Status.BadRequest)
        }
    }
}
