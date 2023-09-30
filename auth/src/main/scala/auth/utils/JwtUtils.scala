package auth.utils

import io.really.jwt._
import play.api.libs.json.Json

import java.time.Instant

object JwtUtils {
  def generateToken(username: String): String = {
    JWT.encode(
      secret = "hereShouldBePrivateKeyNobodyWillKnow",
      payload = Json.obj(
        "name" -> username,
        "iat" -> Instant.now.getEpochSecond
      ),
      algorithm = Some(Algorithm.HS256)
    )
  }
}
