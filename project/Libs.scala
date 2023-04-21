import sbt._

object V {
  val zio = "2.0.13"
  val zioHttp = "0.0.5"

  val pureconfig = "0.17.3"
}


object Libs {

  val zio: List[ModuleID] = List(
    "dev.zio" %% "zio" % V.zio,
    "dev.zio" %% "zio-http" % V.zioHttp
  )

  val pureconfig: List[ModuleID] = List(
    "com.github.pureconfig" %% "pureconfig" % V.pureconfig
  )
}
