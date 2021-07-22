import sbt._

object Dependencies {

  lazy val CommonDependencies = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.scalamock" %% "scalamock" % "5.1.0" % "test",
    "org.mockito" % "mockito-all" % "1.10.19" % Test,
    "dev.zio" %% "zio" % "1.0.9",
    "com.twitter" %% "finagle-http" % "21.6.0",
    "org.apache.commons" % "commons-lang3" % "3.12.0",
    "com.google.code.gson" % "gson" % "2.8.7"
  )


}