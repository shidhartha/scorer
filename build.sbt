name := "scorer"

version := "1.0"

scalaVersion := "2.12.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.23",
  "io.spray" %% "spray-json" % "1.3.5",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.23" % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)