package com.politrons.api

import com.google.gson.Gson
import com.politrons.command.ConvertCommand
import com.politrons.dao.ConversionDAO
import com.politrons.service.ConversionService
import com.politrons.view.Conversion
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.{Logger, LoggerFactory}
import zio.{Runtime, Task, ZIO, ZLayer}

case class ConversionApi(service: ConversionService,
                         dao: ConversionDAO) {

  private val logger: Logger = LoggerFactory.getLogger(classOf[ConversionApi])

  private val gson = new Gson()

  /**
   * Here we define the service with the POST endpoint [/api/convert].
   * Once we receive the request we create a ZIO program that run asynchronously in a Fiber, so there's
   * non blocking request logic, and also we have natural error handling.
   */
  def createService(): Task[Service[Request, Response]] =
    ZIO.effect {
      (req: http.Request) => {
        logger.debug(s"Request param:$req")
        req.method match {
          case Post =>
            req.path match {
              case "/api/convert/" =>
                logger.debug(s"Request uri param:${req.uri}")
                val conversionProgram = (for {
                  conversion <- service.convert()
                  response <- transformCurrencyExchangeIntoJson(req, conversion)
                } yield Future.value(response)).catchAll(t => {
                  logger.error(s"[ConversionApi] Error in conversion request. Caused by ${ExceptionUtils.getStackTrace(t)}")
                  val errorResponse =
                    s"""
                       |{ "message":"${t.getMessage}"}
                       |""".stripMargin
                  val response = Response(req.version, Status.InternalServerError)
                  response.setContentString(errorResponse)
                  response.setContentTypeJson()
                  ZIO.succeed(Future(response))
                })
                val dependencies = ZLayer.succeed(crateConversionCommand(req)) ++ ZLayer.succeed(dao)
                Runtime.global.unsafeRun(conversionProgram.provideLayer(dependencies))
              case _ =>
                Future(Response(req.version, Status.NotImplemented))
            }
        }
      }
    }

  private def crateConversionCommand(req: Request) = {
    gson.fromJson(req.getContentString(), classOf[ConvertCommand])
  }

  private def transformCurrencyExchangeIntoJson(req: Request, conversion: Conversion): Task[Response] = {
    ZIO.effect {
      val response = Response(req.version, Status.Ok)
      response.setContentString(gson.toJson(conversion))
      response.setContentTypeJson()
      response
    }
  }
}
