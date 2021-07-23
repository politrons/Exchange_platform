package com.politrons.service

import com.politrons.service.impl.CurrencyExchangeServiceImpl
import com.politrons.model.CurrencyExchangeRequest
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import zio.{Runtime, ZLayer}

class CurrencyExchangeServiceSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
  }

  override def afterAll(): Unit = {
  }

  feature("Currency Exchange Engine") {

    scenario("GBR to EUR exchange successful") {
      Given("an instance of CurrencyExchangeEngine")
      val engine = CurrencyExchangeServiceImpl()
      When("I create a currencyExchangeProgram passing an exchange from EUR to GBR")
      val dependencies = ZLayer.succeed(CurrencyExchangeRequest("GBR", "EUR", BigDecimal(102.6)))
      val currencyExchangeProgram = engine.exchange()
      val response = Runtime.global.unsafeRun(currencyExchangeProgram.provideLayer(dependencies))
      Then("I receive the info of the exchange properly")
      assert(response.isRight)
      val currencyExchange = response.right.get
      assert(currencyExchange.exchange == "1.11")
      assert(currencyExchange.original == "102.6")
      assert(currencyExchange.amount == "113.886")
    }

    scenario("GBR to Foo exchange error since Foo does not exist") {
      Given("an instance of CurrencyExchangeEngine")
      val engine = CurrencyExchangeServiceImpl()
      When("I create a currencyExchangeProgram passing an exchange from EUR to GBR")
      val dependencies = ZLayer.succeed(CurrencyExchangeRequest("GBR", "Foo", BigDecimal(102.6)))
      val currencyExchangeProgram = engine.exchange()
      val response = Runtime.global.unsafeRun(currencyExchangeProgram.provideLayer(dependencies))
      Then("I receive the info of the exchange properly")
      assert(response.isLeft)
    }
  }
}
