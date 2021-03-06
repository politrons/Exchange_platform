package com.politrons.app

import com.politrons.api.CurrencyExchangeApi
import com.politrons.service.CurrencyExchangeService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, ListeningServer, Service}
import com.twitter.util.Await
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.{Logger, LoggerFactory}
import zio.{Has, Runtime, Task, ZIO, ZLayer, ZManaged}
import com.twitter.conversions.DurationOps._

import scala.concurrent.ExecutionContextExecutor

object CurrencyExchangeServer {

  private val logger: Logger = LoggerFactory.getLogger("CurrencyExchangeServer")

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  private var server: ListeningServer = _

  /**
   * Entry point of the program to be started.
   */
  def main(args: Array[String]): Unit = {
    val port = 9995
    val serverProgram = start(port)
    val engine = CurrencyExchangeService()
    val currencyExchangeApi = CurrencyExchangeApi(engine)
    server = Runtime.global.unsafeRun(serverProgram.provideLayer(ZLayer.succeed(currencyExchangeApi)))
    Await.ready(server)
  }

  /**
   * ZIO program to start up the [Finagle] server in a specific port.
   * We pass a Dependency to the program with [CurrencyExchangeService],
   * to define the endpoints
   */
  def start(port: Int): ZIO[Has[CurrencyExchangeApi], Throwable, ListeningServer] = {
    (for {
      currencyExchangeApi <- ZManaged.service[CurrencyExchangeApi].useNow
      service <- currencyExchangeApi.createService()
      server <- createServer(port, service)
      _ <- ZIO.effect(logger.info(s"[CurrencyExchangeServer] server up and running in port $port"))
    } yield server).catchAll { t =>
      logger.error(s"[CurrencyExchangeServer] Error initializing. Caused by ${ExceptionUtils.getStackTrace(t)}")
      ZIO.fail(t)
    }
  }

  /**
   * We create a [ListeningServer] where we specify the service to route the calls.
   */
  private def createServer(port: Int, service: Service[Request, Response]): Task[ListeningServer] = {
    ZIO.effect {
      Http.server
        .serve(s"0.0.0.0:$port", service)
    }
  }

  def stop(): Unit = {
    if (server != null) Await.result(server.close(0.seconds))
  }
}