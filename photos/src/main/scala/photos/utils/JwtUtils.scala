package photos.utils

import io.really.jwt.{JWT, JWTResult}
import zio.ZIO

sealed trait AuthError
case class JwtError(msg: String) extends Exception(msg) with AuthError

object JwtUtils {
  def verifyJwtToken(jwt: String): ZIO[Any, AuthError, Unit] = {
    val decodedResult =
      JWT.decode(jwt, Some("hereShouldBePrivateKeyNobodyWillKnow"))
    decodedResult match {
      case JWTResult.JWT(_, _) => ZIO.succeed()
      case _                   => ZIO.fail(JwtError("Couldn't parse JWT authorization token"))
    }
  }
}
