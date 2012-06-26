name := "psug"

version := "1.0"

scalaVersion := "2.9.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
    "com.typesafe.akka" % "akka-actor" % "2.0.2", 
    "com.typesafe.akka" % "akka-remote" % "2.0.2",
    "com.typesafe.akka" % "akka-kernel" % "2.0.2",
    "org.specs2" %% "specs2" % "1.11" % "test"
)
