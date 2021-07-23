package com.politrons.service

import com.politrons.command.ConvertCommand
import com.politrons.dao.ConversionDAO
import com.politrons.view.Conversion
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.{Logger, LoggerFactory}
import zio.{Has, ZIO, ZManaged}

case class ConversionService() {

  private val logger: Logger = LoggerFactory.getLogger(classOf[ConversionService])

  /**
   * This ZIO program receive in the evaluation time the dependencies they needs. The [ConversionDAO] of the communication
   * and the command [ConvertCommand] with all the information to be passed to the DAO.
   * We return a [Future] with the response body
   */
  def convert(): ZIO[Has[ConversionDAO] with Has[ConvertCommand], Throwable, Conversion] = {
    (for {
      dao <- ZManaged.service[ConversionDAO].useNow
      command <- ZManaged.service[ConvertCommand].useNow
      currencyExchange <- dao.convert(command.fromCurrency, command.toCurrency, command.amount)
    } yield currencyExchange).catchAll(t => {
      logger.error(s"[createConversionRequestProgram] Error in conversion with DAO. Caused by ${ExceptionUtils.getStackTrace(t)}")
      ZIO.fail(t)
    })
  }
}
