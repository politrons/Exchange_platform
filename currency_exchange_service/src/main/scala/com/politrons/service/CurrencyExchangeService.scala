package com.politrons.service

import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import com.politrons.model.CurrencyExchangeRequest
import com.politrons.view.CurrencyExchange
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.{Logger, LoggerFactory}
import zio.{Has, URIO, ZIO, ZManaged}

import java.util.concurrent.TimeUnit

case class CurrencyExchangeService() {

  private val logger: Logger = LoggerFactory.getLogger(classOf[CurrencyExchangeService])

  //TODO:Feedback - Add cache with Caffeine
  val cache: Cache[String, CurrencyExchange] = Caffeine.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES)
    .maximumSize(100)
    .build()

  val currencyExchanges: Map[String, BigDecimal] = Map[String, BigDecimal](
    "GBR-EUR" -> BigDecimal(1.11),
    "EUR-GBR" -> BigDecimal(0.98),
    "EUR-USD" -> BigDecimal(0.87),
    "USD-EUR" -> BigDecimal(1.2)
  )

  /**
   * Function that receive as dependency the Request of the call, we extract the
   * query params, we make the calc and we return an Either with error channel Throwable
   * and succeed channel BigDecimal.
   */
  def exchange(): URIO[Has[CurrencyExchangeRequest], Either[Throwable, CurrencyExchange]] = {
    (for {
      request <- ZManaged.service[CurrencyExchangeRequest].useNow
      //TODO:Feedback - Add cache with Caffeine
      exchange <- ZIO.fromOption(Option(cache.getIfPresent(s"${request.from}-${request.to}")))
        .catchAll(_ => {
          ZIO.effect {
            val exchange = currencyExchanges(s"${request.from}-${request.to}")
            val originalAmount = request.amount
            val amount = exchange * originalAmount
            val currencyExchange = CurrencyExchange(exchange.toString(), amount.toString(), originalAmount.toString())
            cache.put(s"${request.from}-${request.to}", currencyExchange)
            currencyExchange
          }
        })
    } yield exchange).catchAll(t => {
      logger.error(s"Error in currency exchange. Caused by ${ExceptionUtils.getStackTrace(t)}")
      ZIO.fail(t)
    }).either
  }
}
