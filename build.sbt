import Dependencies.CommonDependencies

name := "exchange_platform"

version := "0.1"

scalaVersion := "2.12.14"

lazy val commonSettings = Seq(
  organization := "com.politrons",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.8",
  scalacOptions := Seq(
    "-deprecation",
    "-feature"
  ),
  libraryDependencies ++= CommonDependencies
)

lazy val conversion_service = project
  .in(file("conversion_service"))
  .settings(
    name := "conversion_service",
    assembly / test := (Test / test).value,
    commonSettings
  )

lazy val currency_exchange_service = project
  .in(file("currency_exchange_service"))
  .settings(
    name := "currency_exchange_service",
    assembly / test := (Test / test).value,
    commonSettings
  )

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)