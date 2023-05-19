package auth.utils

import io.really.jwt._
import play.api.libs.json.Json

object JwtUtils {
  def generateToken(username: String): String = {
    JWT.encode(
      secret = "secretKey",
      payload = Json.obj("username" -> username),
      algorithm = Some(Algorithm.HS256))
  }
}
