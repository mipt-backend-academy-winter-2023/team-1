import Libs._
import sbt._

trait Dependencies {
  def dependencies: Seq[ModuleID]
}

object Dependencies {

  object Auth extends Dependencies {
    override def dependencies: Seq[ModuleID] = Seq(zio, pureconfig).flatten
  }

  object Routing extends Dependencies {
    override def dependencies: Seq[ModuleID] = Seq(zio, pureconfig).flatten
  }

  object Helper extends Dependencies {
    override def dependencies: Seq[ModuleID] = Seq(zio, pureconfig).flatten
  }
}