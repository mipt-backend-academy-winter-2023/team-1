package routing.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object JsonProtocol {
  implicit val routingRequestDecoder: Decoder[RoutingRequest] = deriveDecoder
  implicit val routingRequestEncoder: Encoder[RoutingRequest] = deriveEncoder
  implicit val jamIdDecoder: Decoder[JamId] = deriveDecoder
  implicit val jamIdEncoder: Encoder[JamId] = deriveEncoder
}
