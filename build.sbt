name := "stylecookbook"

version := "1.0"

scalaVersion := "2.11.2"

scalacOptions ++= Seq("-feature","-language:implicitConversions")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "org.scalacheck"%% "scalacheck" % "1.11.5" % "test"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.1"

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
