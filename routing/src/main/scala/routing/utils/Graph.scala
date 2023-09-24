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

  private val vertices = new mutable.HashSet[Int]()
  private val edgesFrom = new mutable.HashMap[Int, Seq[Edge]]()
  private val distances = new mutable.HashMap[Int, Float]()
  private val prev = new mutable.HashMap[Int, Tuple2[Street, GeoPoint]]()

  def reload: ZIO[CrossroadRepository with BuildingRepository with StreetRepository, Throwable, Unit] = {
    geoPoints.clear()
    streets.clear()

    for {
      _ <- CrossroadRepository.findAllCrossroads.runCollect.map(points => geoPoints ++= points.toArray)
      _ <- BuildingRepository.findAllBuildings.runCollect.map(points => geoPoints ++= points.toArray)
      _ <- StreetRepository.findAllStreets.runCollect.map(foundStreets => streets ++= foundStreets.toArray)
      _ = init()
    } yield ()
  }

  def searchForShortestRoute(routingRequest: RoutingRequest): ZIO[Any, Throwable, Seq[Geo]] = {
    var queue = new mutable.PriorityQueue[Tuple2[Float, Int]]()

    distances(routingRequest.fromPointId) = 0
    queue.addOne((0, routingRequest.fromPointId))

    while (queue.nonEmpty) {
      val (currentDist, currentVert) = queue.head
      queue = queue.drop(1)
      if (currentDist <= distances(currentVert)) {
        for (edge <- edgesFrom(currentVert)) {
          if (distances(edge.geoPointTo.id) > distances(currentVert) + edge.distance) {
            distances(edge.geoPointTo.id) = distances(currentVert) + edge.distance
            prev.addOne((edge.geoPointTo.id, (edge.toStreet, edge.geoPointFrom)))
            queue.addOne((distances(edge.geoPointTo.id), edge.geoPointTo.id))
          }
        }
      }
    }

    if (distances(routingRequest.toPointId) < Float.PositiveInfinity - 1)
      ZIO.succeed(collectFullRoute(routingRequest))
    else
      ZIO.fail(RouteNotFound("Route not found"))
  }

  // -------------------------------------------------------------------------------------------------------------------

  private def init(): Unit = {
    vertices.clear()
    edgesFrom.clear()
    distances.clear()
    prev.clear()

    geoPoints.foreach(Graph.addPoint)
    streets.foreach(Graph.addEdge)
    vertices.foreach {
      vertex => distances.addOne((vertex, Float.PositiveInfinity))
    }
  }

  private def addPoint(geoPoint: GeoPoint): Unit = vertices.addOne(geoPoint.id)

  private def addEdge(street: Street): Unit = {
    // TODO: подумать про невалидные точки
    val geoPointFrom = geoPoints.find(_.id == street.fromId).get
    val geoPointTo = geoPoints.find(_.id == street.toId).get
    val weight: Float = calculateDistance(
      geoPointFrom.longitude,
      geoPointFrom.latitude,
      geoPointTo.longitude,
      geoPointTo.latitude)

    edgesFrom(geoPointFrom.id) =
      Edge(geoPointFrom, geoPointTo, street.name, weight) +: edgesFrom.getOrElse(geoPointFrom.id, Seq())
    edgesFrom(geoPointTo.id) =
      Edge(geoPointTo, geoPointFrom, street.name, weight) +: edgesFrom.getOrElse(geoPointTo.id, Seq())
  }

  // TODO: научиться считать в километрах
  private def calculateDistance(longitude1: Float, latitude1: Float, Longitude2: Float, latituse2: Float): Float = ???

  private def collectFullRoute(routingRequest: RoutingRequest): Seq[Geo] = {
    val endPointId = routingRequest.toPointId
    var route: Seq[Geo] = Seq(geoPoints.find(_.id == endPointId).get)
    var (street, point) = prev(endPointId)

    while (point.id != routingRequest.fromPointId) {
      route = point +: street +: route
      val prevRoute = prev(point.id)
      street = prevRoute._1
      point = prevRoute._2
    }

    point +: street +: route
  }
}
