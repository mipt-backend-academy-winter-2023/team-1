package routing.utils

import routing.model.{Geo, GeoPoint, RoutingRequest, Street}
import routing.repository.{BuildingRepository, CrossroadRepository, StreetRepository}
import zio.ZIO

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Graph {
  case class RouteNotFound(msg: String) extends Exception(msg)
  case class Edge(geoPointFrom: GeoPoint, geoPointTo: GeoPoint, streetName: String, distance: Float) {
    def toStreet: Street = Street(geoPointFrom.id, geoPointFrom.id, streetName)
  }

  private val geoPoints = new ArrayBuffer[GeoPoint]()
  private val streets = new ArrayBuffer[Street]()

  private val graphEdges = new mutable.HashMap[Int, Seq[Edge]]()
  private val distances = new mutable.HashMap[Int, Float]()
  private val prev = new mutable.HashMap[Int, Tuple2[Street, GeoPoint]]()

  def reload: ZIO[CrossroadRepository with BuildingRepository with StreetRepository, Throwable, Unit] = {
    geoPoints.clear()
    streets.clear()
    for {
      _ <- CrossroadRepository.findAllCrossroads.runCollect.map(points => geoPoints ++= points.toList)
      _ <- BuildingRepository.findAllBuildings.runCollect.map(points => geoPoints ++= points.toList)
      _ <- StreetRepository.findAllStreets.runCollect.map(foundStreets => streets ++= foundStreets.toList)
      _ = init()
    } yield ()
  }

  private implicit val ordering: Ordering[Tuple2[Float, Int]] =
    (point1, point2) => if (point1._1 < point2._1) 1 else 0

  def searchForShortestRoute(routingRequest: RoutingRequest): ZIO[Any, Throwable, Seq[Geo]] = {
    distances.clear()
    prev.clear()

    var queue = new mutable.PriorityQueue[Tuple2[Float, Int]]()
    queue.addOne((0, routingRequest.fromPointId))
    distances(routingRequest.fromPointId) = 0

    while (queue.nonEmpty) {
      val (currentDist, currentVert) = queue.head
      queue = queue.drop(1)
      if (currentDist <= distances(currentVert)) {
        for (edge <- graphEdges(currentVert)) {
          val distanceToCurrentVert = distances(currentVert)
          if (distances(edge.geoPointTo.id) > distanceToCurrentVert + edge.distance) {
            distances(edge.geoPointTo.id) = distanceToCurrentVert + edge.distance
            prev.addOne((edge.geoPointTo.id, (edge.toStreet, edge.geoPointFrom)))
            queue.addOne((distanceToCurrentVert + edge.distance, edge.geoPointTo.id))
          }
        }
      }
    }

    if (distances(routingRequest.toPointId) < Float.PositiveInfinity - 1)
      ZIO.succeed(collectFullRoute(routingRequest))
    else
      ZIO.fail(RouteNotFound("Route not found"))
  }

  private def init(): Unit = {
    graphEdges.clear()
    distances.clear()
    prev.clear()
    streets.foreach(addEdge)
  }

  private def findGeoPointById(id: Int): GeoPoint = geoPoints.find(_.id == id).get

  def findGeoPointByIdSafe(id: Int): ZIO[Any, Throwable, GeoPoint] = geoPoints.find(_.id == id) match {
    case Some(geoPoint) => ZIO.succeed(geoPoint)
    case _ => ZIO.fail(RouteNotFound("Route not found"))
  }

  private def addEdge(street: Street): Unit = {
    val geoPointFrom = findGeoPointById(street.fromId)
    val geoPointTo = findGeoPointById(street.toId)
    val weight: Float = calculateDistance(
      geoPointFrom.latitude,
      geoPointFrom.longitude,
      geoPointTo.latitude,
      geoPointTo.longitude)
    graphEdges(geoPointFrom.id) =
      Edge(geoPointFrom, geoPointTo, street.name, weight) +: graphEdges.getOrElse(geoPointFrom.id, Seq())
    graphEdges(geoPointTo.id) =
      Edge(geoPointTo, geoPointFrom, street.name, weight) +: graphEdges.getOrElse(geoPointTo.id, Seq())
  }

  private def calculateDistance(longitude1: Float, latitude1: Float, longitude2: Float, latitude2: Float): Float = {
    val theta = longitude1 - longitude2
    val distance = 60 * 1.1515 * (180 / Math.PI) * Math.acos(
      Math.sin(latitude1 * (Math.PI / 180)) *
      Math.sin(latitude2 * (Math.PI / 180)) +
      Math.cos(latitude1 * (Math.PI / 180)) *
      Math.cos(latitude2 * (Math.PI / 180)) *
      Math.cos(theta * (Math.PI / 180)))
    (distance * 1.609344).toFloat
  }

  private def collectFullRoute(routingRequest: RoutingRequest): Seq[Geo] = {
    val endGeoPoint = findGeoPointById(routingRequest.toPointId)
    var route: Seq[Geo] = Seq(endGeoPoint)
    var (street, point) = prev(routingRequest.toPointId)

    while (point.id != routingRequest.fromPointId) {
      route = point +: street +: route
      val prevRoute = prev(point.id)
      street = prevRoute._1
      point = prevRoute._2
    }
    point +: street +: route
  }
}
