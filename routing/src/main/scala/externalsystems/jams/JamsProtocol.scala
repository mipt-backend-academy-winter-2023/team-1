package externalsystems.jams

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

object JamsProtocol {
  case class JamsResponse(jamValue: Int)

  implicit val jamsResponseDecoder: Decoder[JamsResponse] = deriveDecoder
}
