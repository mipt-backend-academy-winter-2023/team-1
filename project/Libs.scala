import sbt._

object V {
  object zio {
    val version = "2.0.13"
    val http = "0.0.5"
    val postgresql = "0.1.2"
  }

  val pureconfig = "0.17.4"
  val flyway = "9.16.0"
  val circe = "0.14.1"
}


object Libs {

  val zio: List[ModuleID] = List(
    "dev.zio" %% "zio" % V.zio.version,
    "dev.zio" %% "zio-http" % V.zio.http,
    "dev.zio" %% "zio-sql-postgres" % V.zio.postgresql,
  )

  val pureconfig: List[ModuleID] = List(
    "com.github.pureconfig" %% "pureconfig" % V.pureconfig,
  )

  val flyway: List[ModuleID] = List(
    "org.flywaydb" % "flyway-core" % V.flyway,
  )

  val circe: List[ModuleID] = List(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
  ).map(_ % V.circe)
}
