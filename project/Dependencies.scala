import sbt._

object Dependencies {

  lazy val CommonDependencies = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "org.scalamock" %% "scalamock" % "5.1.0" % Test,
    "org.mockito" % "mockito-all" % "1.10.19" % Test,
    "dev.zio" %% "zio" % "1.0.9" % Compile,
    "com.twitter" %% "finagle-http" % "21.6.0" % Compile,
    "org.apache.commons" % "commons-lang3" % "3.12.0" % Compile,
    "com.google.code.gson" % "gson" % "2.8.7" % Compile
  )


}