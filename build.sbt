import Dependencies.{Auth, Routing, Photos, Helper}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "project-mipt"
  )
  .aggregate(
    auth,
    routing,
    helper,
  )
  .dependsOn(
    auth,
    routing,
    helper,
  )

lazy val auth = (project in file("auth"))
  .settings(
    name := "project-auth",
    libraryDependencies ++= Auth.dependencies
  )

lazy val routing = (project in file("routing"))
  .settings(
    name := "project-routing",
    libraryDependencies ++= Routing.dependencies
  )

lazy val photos = (project in file("photos"))
  .settings(
    name := "project-photos",
    libraryDependencies ++= Photos.dependencies
  )

lazy val helper = (project in file("helper"))
  .settings(
    name := "project-helper",
    libraryDependencies ++= Helper.dependencies
  )

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", xs@_*) =>
    xs.map(_.toLowerCase) match {
      case "manifest.mf" :: Nil |
           "index.list" :: Nil |
           "dependencies" :: Nil |
           "license" :: Nil |
           "notice" :: Nil => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case x => (ThisBuild / assemblyMergeStrategy).value(x)
}


