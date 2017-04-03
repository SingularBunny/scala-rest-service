name := "rest"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "io.spray" % "spray-can_2.11" % "1.3.4",
  "io.spray" % "spray-http_2.11" % "1.3.4",
  "io.spray" % "spray-routing_2.11" % "1.3.4",
  "net.liftweb" % "lift-json_2.11" % "3.1.0-M1",
  "com.typesafe.slick" % "slick_2.11" % "3.2.0",
  "mysql" % "mysql-connector-java" % "5.1.23",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.9",
  "com.typesafe.akka" % "akka-slf4j_2.11" % "2.3.9",
  "ch.qos.logback" % "logback-classic" % "1.2.1",
  "com.h2database" % "h2" % "1.4.194",
  "io.spray" % "spray-testkit_2.11" % "1.3.4" % "test",
  "org.specs2" %% "specs2-core" % "2.5" % "test"
)

resolvers ++= Seq(
  "Spray repository" at "http://repo.spray.io",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

scalacOptions in Test ++= Seq("-Yrangepos")