package auth.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import zio.schema.{DeriveSchema, Schema}

object JsonProtocol {
  implicit val customerDecoder: Decoder[User] = deriveDecoder
  implicit val customerEncoder: Encoder[User] = deriveEncoder

  implicit val customerSchema: Schema[User] = DeriveSchema.gen[User]
}

