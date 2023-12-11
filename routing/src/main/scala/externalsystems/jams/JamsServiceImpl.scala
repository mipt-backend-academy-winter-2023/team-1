package externalsystems.jams

import externalsystems.jams.JamsProtocol.JamsResponse
import sttp.client3.circe.asJson
import sttp.client3.{SttpBackend, basicRequest}
import sttp.model.Uri
import zio.{IO, Task, ZIO, ZLayer}

class JamsServiceImpl(httpClient: SttpBackend[Task, Any]) extends JamsService {

  override def getJamsValue(pointId: Int): IO[Serializable, JamsResponse] =
    httpClient
      .send(
        basicRequest
          .get(baseUri.resolve(Uri(s"$pointId")))
          .response(asJson[JamsResponse])
      )
      .flatMap(jamsResponse => ZIO.fromEither(jamsResponse.body))

  private val baseUri: Uri = Uri("http://jams:8080/jam/")
}

object JamsServiceImpl {
  val live: ZLayer[SttpBackend[Task, Any], Nothing, JamsService] =
    ZLayer.fromFunction(new JamsServiceImpl(_))
}
