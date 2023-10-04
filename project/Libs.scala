import sbt._

object V {
  val zio = "2.0.13"
  val zioHttp = "0.0.5"
  val zioSql = "0.1.2"
  val zioNio = "2.0.1"
  val pureconfig = "0.17.2"
  val flyway = "9.16.0"
  val circe = "0.14.1"
  val jwt = "1.8.1"
}

object Libs {

  val zio: List[ModuleID] = List(
    "dev.zio" %% "zio" % V.zio,
    "dev.zio" %% "zio-http" % V.zioHttp,
    "dev.zio" %% "zio-sql-postgres" % V.zioSql,
    "dev.zio" %% "zio-nio" % V.zioNio
  )

  val pureconfig: List[ModuleID] = List(
    "com.github.pureconfig" %% "pureconfig" % V.pureconfig
  )

  val flyway: List[ModuleID] = List(
    "org.flywaydb" % "flyway-core" % V.flyway
  )

  val circe: List[ModuleID] = List(
    "io.circe" %% "circe-core" % V.circe,
    "io.circe" %% "circe-generic" % V.circe,
    "io.circe" %% "circe-parser" % V.circe
  )

  val jwt: List[ModuleID] = List(
    "com.github.xuwei-k" %% "jwt-scala" % V.jwt
  )
}
