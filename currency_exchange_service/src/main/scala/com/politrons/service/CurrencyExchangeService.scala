package com.politrons.service

import com.politrons.model.CurrencyExchangeRequest
import com.politrons.view.CurrencyExchange
import zio.{Has, URIO}

trait CurrencyExchangeService {
  def exchange(): URIO[Has[CurrencyExchangeRequest], Either[Throwable, CurrencyExchange]]
}
