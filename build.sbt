name := """scaladays-cal"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies += "org.mnode.ical4j" % "ical4j" % "1.0.5.2"

libraryDependencies += "org.jsoup" % "jsoup" % "1.7.2"
