//import AssemblyKeys._

name := "kumquat"

version := "0.0"

scalaVersion := "2.10.4"

val sprayVersion = "1.3+"
val akkaVersion = "2.3+"

//assemblySettings

resolvers ++= List(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray repo" at "http://repo.spray.io"
)

scalacOptions ++= Seq("-feature", "-language:postfixOps")

libraryDependencies ++= List(
  "org.scalatest" %% "scalatest" % "latest.integration" % "test",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "io.spray" %  "spray-can" % sprayVersion,
  "io.spray" %  "spray-routing" % sprayVersion
)

