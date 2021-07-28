import sbt.{Compile, _}

object Dependencies {

  lazy val CommonDependencies = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "org.scalamock" %% "scalamock" % "5.1.0" % Test,
    "org.mockito" % "mockito-all" % "1.10.19" % Test,
    "dev.zio" %% "zio" % "1.0.9" % Compile,
    "com.twitter" %% "finagle-http" % "21.6.0" % Compile,
    "org.apache.commons" % "commons-lang3" % "3.12.0" % Compile,
    "com.google.code.gson" % "gson" % "2.8.7" % Compile,
    //    TODO:Feedback forgot to add the dependencies :_(
    "ch.qos.logback" % "logback-core" % "1.2.3" % Compile,
    "ch.qos.logback" % "logback-classic" % "1.2.3" % Compile,
    //TODO:Add caffeine for cache
    "com.github.ben-manes.caffeine" % "caffeine" % "2.8.8" % Compile
  )


}