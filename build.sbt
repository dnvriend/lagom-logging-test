organization in ThisBuild := "com.github.dnvriend"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val `lagom-logging-test` = (project in file("."))
  .aggregate(`lagom-logging-test-api`, `lagom-logging-test-impl`)

lazy val `lagom-logging-test-api` = (project in file("lagom-logging-test-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `lagom-logging-test-impl` = (project in file("lagom-logging-test-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`lagom-logging-test-api`)
