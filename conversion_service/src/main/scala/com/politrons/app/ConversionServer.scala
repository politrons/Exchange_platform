package com.politrons.app

import com.politrons.api.ConversionApi
import com.politrons.dao.ConversionDAO
import com.politrons.service.ConversionService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, ListeningServer, Service}
import com.twitter.util.Await
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.{Logger, LoggerFactory}
import zio.{Has, Runtime, Task, ZIO, ZLayer, ZManaged}
import com.twitter.conversions.DurationOps._

import scala.concurrent.ExecutionContextExecutor

object ConversionServer {

  private val logger: Logger = LoggerFactory.getLogger("ConversionServer")

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  /**
   * Entry point of the program to be started.
   */
  def main(args: Array[String]): Unit = {
    val port = 9994
    val serverProgram = start(port)
    val service = ConversionService()
    val repository = ConversionDAO()
    val conversionApi = ConversionApi(service, repository)
    Runtime.global.unsafeRun(serverProgram.provideLayer(ZLayer.succeed(conversionApi)))
  }

  /**
   * ZIO program to start up the [Finagle] server in a specific port.
   * We pass a Dependency to the program with [ConversionApi],
   * to define the endpoints.
   */
  def start(port: Int): ZIO[Has[ConversionApi], Throwable, Unit] = {
    (for {
      conversionApi <- ZManaged.service[ConversionApi].useNow
      service <- conversionApi.createService()
      server <- createServer(port, service)
      _ <- ZIO.effect(Await.ready(server))
    } yield logger.info(s"[ConversionServer] server up and running in port $port")).catchAll { t =>
      logger.error(s"[ConversionServer] Error initializing. Caused by ${ExceptionUtils.getStackTrace(t)}")
      ZIO.fail(t)
    }
  }

  /**
   * We create a [ListeningServer] where we specify the operator [withRequestTimeout(5.seconds)]
   * Allowing close the communication after that period of time.
   */
  private def createServer(port: Int, service: Service[Request, Response]): Task[ListeningServer] = {
    ZIO.effect {
      Http.server
        .withRequestTimeout(5.seconds)
        .serve(s"0.0.0.0:$port", service)
    }
  }
}

