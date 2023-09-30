package routing.model

trait Geo

trait GeoPoint extends Geo {
  def id: Int
  def longitude: Float
  def latitude: Float
}

case class Crossroad(
  id: Int,
  longitude: Float,
  latitude: Float
) extends GeoPoint

case class Building(
  id: Int,
  longitude: Float,
  latitude: Float,
  name: String
) extends GeoPoint

case class Street(
  fromId: Int,
  toId: Int,
  name: String
) extends Geo

case class RoutingRequest(
  fromPointId: Int,
  toPointId: Int
)
