import Libs._
import sbt._

trait Dependencies {
  def dependencies: Seq[ModuleID]
}

object Dependencies {

  object Auth extends Dependencies {
    override def dependencies: Seq[ModuleID] =
      Seq(zio, pureconfig, flyway, circe, jwt).flatten
  }

  object Routing extends Dependencies {
    override def dependencies: Seq[ModuleID] =
      Seq(zio, pureconfig, flyway, circe, jwt, sttp, rezilience).flatten
  }

  object Photos extends Dependencies {
    override def dependencies: Seq[ModuleID] =
      Seq(zio, pureconfig, circe, jwt).flatten
  }

  object Helper extends Dependencies {
    override def dependencies: Seq[ModuleID] = Seq(zio, pureconfig).flatten
  }
}
