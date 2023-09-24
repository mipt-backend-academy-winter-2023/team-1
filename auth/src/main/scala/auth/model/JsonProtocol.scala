package auth.model

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

object JsonProtocol {
  implicit val customerDecoder: Decoder[User] = deriveDecoder
  implicit val customerEncoder: Encoder[User] = deriveEncoder
}
