package jams

import routing.model.JamId
import routing.model.JsonProtocol.jamIdDecoder
import sttp.client3.{SttpBackend, basicRequest}
import sttp.client3.circe.asJson
import sttp.model.Uri
import zio.{Task, ZIO, IO, ZLayer}

trait Jams {
  def getJamId(id: Int): IO[Serializable, JamId]
}

object Jams {
  def getJamId(id: Int): ZIO[Jams, Serializable, JamId] =
    ZIO.serviceWithZIO[Jams](_.getJamId(id))
}

class JamsImpl(
    httpClient: SttpBackend[Task, Any]
) extends Jams {
  override def getJamId(id: Int): Task[JamId] = {
    val port = 8080
    val req = basicRequest
      .get(Uri(s"http://jams:$port/jam/$id"))
      .response(asJson[JamId])
    httpClient.send(req).flatMap(resp => ZIO.fromEither(resp.body))
  }
}

object JamsImpl {
  val live: ZLayer[SttpBackend[Task, Any], Nothing, Jams] =
    ZLayer.fromFunction(new JamsImpl(_))
}
