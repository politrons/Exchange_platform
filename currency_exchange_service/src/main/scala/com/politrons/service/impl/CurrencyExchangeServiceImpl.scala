package com.politrons.service.impl

import com.politrons.service.CurrencyExchangeService
import com.politrons.model.CurrencyExchangeRequest
import com.politrons.view.CurrencyExchange
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.{Logger, LoggerFactory}
import zio.{Has, URIO, ZIO, ZManaged}

case class CurrencyExchangeServiceImpl() extends CurrencyExchangeService {

  private val logger: Logger = LoggerFactory.getLogger(classOf[CurrencyExchangeServiceImpl])

  val currencyExchange: Map[String, BigDecimal] = Map[String, BigDecimal](
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
  override def exchange(): URIO[Has[CurrencyExchangeRequest], Either[Throwable, CurrencyExchange]] = {
    (for {
      request <- ZManaged.service[CurrencyExchangeRequest].useNow
      exchange <- ZIO.effect {
        val exchange = currencyExchange(s"${request.from}-${request.to}")
        val originalAmount = request.amount
        val amount = exchange * originalAmount
        CurrencyExchange(exchange.toString(), amount.toString(), originalAmount.toString())
      }
    } yield exchange).catchAll(t => {
      logger.error(s"Error in currency exchange. Caused by ${ExceptionUtils.getStackTrace(t)}")
      ZIO.fail(t)
    }).either

  }
}
