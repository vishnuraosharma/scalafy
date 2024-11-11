ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "spotify"
  )

libraryDependencies ++= Seq(
  "io.spray" %% "spray-json" % "1.3.6",
  "org.json4s" %% "json4s-native" % "4.0.3",
  "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M6"
)