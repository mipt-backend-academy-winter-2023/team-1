package routing.utils

import io.really.jwt.{JWT, JWTResult}
import zio.ZIO

case class InvalidAuthorizationToken(msg: String) extends Exception(msg)

object JwtUtils {
  def verifyJwtToken(jwt: String): ZIO[Any, Throwable, Unit] = {
    val decodedResult = JWT.decode(jwt, Some("hereShouldBePrivateKeyNobodyWillKnow"))
    decodedResult match {
      case JWTResult.JWT(_, _) => ZIO.succeed()
      case _ => ZIO.fail(InvalidAuthorizationToken("Couldn't parse JWT authorization token"))
    }
  }
}
