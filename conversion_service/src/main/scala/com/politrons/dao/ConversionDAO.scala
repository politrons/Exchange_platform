package com.politrons.dao

import com.google.gson.Gson
import com.politrons.view.Conversion
import com.twitter.finagle.http.{Request, RequestBuilder, Response}
import com.twitter.finagle.service.RetryBudget
import com.twitter.finagle.{Backoff, Http, Service}
import com.twitter.util.{Duration, Monitor, Future => TwitterFuture}
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.{Logger, LoggerFactory}
import zio.{Task, ZIO}

import java.net.URL
import java.util.concurrent.TimeUnit
import scala.concurrent.{Promise, Future => ScalaFuture}

case class ConversionDAO() {

  private val logger: Logger = LoggerFactory.getLogger("ConversionDAO")

  private val gson = new Gson()
  private val endpoint = "api/v1/convert"

  /**
   * Observability, we create a monitor to be invoked in case of runtime errors in coimmunication.
   * Here we could inform third party Observability systems, to apply some strategies.
   */
  private val monitor: Monitor = (t: Throwable) => {
    logger.error(s"Error in Finagle communication. Caused by ${ExceptionUtils.getStackTrace(t)}")
    false
  }

  /**
   * Retry strategy configuration of Finagle client, where we describe the min number of retries per sec,
   * the percentage in which cases we can retry, and finally the timeout of each request.
   */
  val budget: RetryBudget = RetryBudget(
    ttl = Duration(2, TimeUnit.SECONDS),
    minRetriesPerSec = 2,
    percentCanRetry = 0.1
  )

  /**
   * Http client configuration where we define the observability, retry strategy, http2.0 transport
   * protocol, and backoff between each retries.
   * Finagle by default implement circuit breaker, so unless we want to change the configuration or
   * disable this feature, there's nothing we have to configure.
   */
  private val client: Service[Request, Response] =
    Http
      .client
      .withMonitor(monitor)
      .withHttp2
      .withRetryBudget(budget)
      .withRetryBackoff(Backoff.const(Duration(2, TimeUnit.SECONDS)))
      .newService(s"localhost:9995")

  /**
   * Function to calc the CurrencyExchange from one coin to another.
   **/
  def convert(fromCurrency: String,
              toCurrency: String,
              amount: Double): ZIO[Any, Throwable, Conversion] = {
    (for {
      request <- createRequest(fromCurrency, toCurrency, amount)
      response <- ZIO.fromFuture(_ => transformFutures(client(request)))
      responseJson <- getJsonFromResponse(response)
      currencyExchange <- transformToCurrencyExchange(responseJson)
    } yield currencyExchange).catchAll(t => {
      logger.error(s"[ConversionDAO] Error obtaining exchange from external service. Caused by ${ExceptionUtils.getStackTrace(t)}")
      ZIO.fail(t)
    })
  }

   private def getJsonFromResponse(response: Response): Task[String] = {
    ZIO.effect {
      response.statusCode match {
        case 200 => response.getContentString()
        case _ => throw new IllegalStateException(s"Error response ${response.statusCode} from CurrencyExchange server")
      }
    }
  }

  /**
   * Transform the Response into CurrencyExchange controlling all possible side-effects.
   */
  private def transformToCurrencyExchange(jsonResponse: String): Task[Conversion] = {
    ZIO.effect {
      gson.fromJson(jsonResponse, classOf[Conversion])
    }
  }

  /**
   * Create a Finagle Http Request using the command and controlling all side-effects.
   */
  private def createRequest(fromCurrency: String,
                            toCurrency: String,
                            amount: Double): Task[Request] = {
    ZIO.effect {
      RequestBuilder()
        .url(new URL(s"http://localhost:9995/$endpoint" +
          s"?from=$fromCurrency" +
          s"&to=$toCurrency" +
          s"&amount=$amount"))
        .buildGet()
    }
  }

  /**
   * Transform a Twitter Future into Scala Future
   */
  def transformFutures(twitterFuture: TwitterFuture[Response]): ScalaFuture[Response] = {
    val promise = Promise[Response]
    twitterFuture.onSuccess(response => promise.success(response))
    twitterFuture.onFailure(t => promise.failure(t))
    promise.future
  }
}
