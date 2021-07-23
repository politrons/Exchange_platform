package com.politrons.api

import com.google.gson.Gson
import com.politrons.model.CurrencyExchangeRequest
import com.politrons.service.CurrencyExchangeService
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.{Logger, LoggerFactory}
import zio.{Runtime, Task, ZIO, ZLayer}

case class CurrencyExchangeApi(currencyExchangeEngine: CurrencyExchangeService) {

  private val logger: Logger = LoggerFactory.getLogger(classOf[CurrencyExchangeApi])

  private val gson = new Gson()


  /**
   * Here we define the service with the endpoint [/api/v1/convert] which expect some query params.
   * Once we receive the request we create a ZIO program.
   */
  def createService(): Task[Service[Request, Response]] =
    ZIO.effect {
      (req: http.Request) => {
        logger.debug(s"Request uri param:${req}")
        req.path match {
          case "/api/v1/convert" =>
            val request = CurrencyExchangeRequest(
              req.getParam("from"),
              req.getParam("to"),
              BigDecimal(req.getParam("amount")))
            val dependencies = ZLayer.succeed(request)
            val currencyExchangeProgram = currencyExchangeEngine.exchange()
            Runtime.global.unsafeRun(currencyExchangeProgram.provideLayer(dependencies)) match {
              case Right(exchange) =>
                val response = Response(req.version, Status.Ok)
                response.setContentTypeJson()
                response.setContentString(gson.toJson(exchange))
                Future(response)
              case Left(t) =>
                logger.error(s"Error in CurrencyExchangeApi. Caused by ${ExceptionUtils.getStackTrace(t)}")
                Future.value(Response(req.version, Status.InternalServerError))
            }
        }
      }
    }

}
