package com.politrons.service

import com.politrons.model.CurrencyExchangeRequest
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import zio.{Runtime, ZLayer}

class CurrencyExchangeServiceSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  feature("Currency Exchange Engine") {

    scenario("GBR to EUR exchange successful") {
      Given("an instance of CurrencyExchangeEngine")
      val engine = CurrencyExchangeService()
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
      val engine = CurrencyExchangeService()
      When("I create a currencyExchangeProgram passing an exchange from EUR to GBR")
      val dependencies = ZLayer.succeed(CurrencyExchangeRequest("GBR", "Foo", BigDecimal(102.6)))
      val currencyExchangeProgram = engine.exchange()
      val response = Runtime.global.unsafeRun(currencyExchangeProgram.provideLayer(dependencies))
      Then("I receive the info of the exchange properly")
      assert(response.isLeft)
    }

    //TODO:Feedback - Add cache with Caffeine
    scenario("GBR to EUR exchange cache successful") {
      Given("an instance of CurrencyExchangeEngine")
      val engine = CurrencyExchangeService()
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
      And("I make another call to the program and the response is the cache.")
      val dependencies1 = ZLayer.succeed(CurrencyExchangeRequest("GBR", "EUR", BigDecimal(500)))
      val currencyExchangeProgram1 = engine.exchange()
      val response1 = Runtime.global.unsafeRun(currencyExchangeProgram.provideLayer(dependencies))
      val currencyExchange1 = response1.right.get
      assert(currencyExchange1.exchange == "1.11")
      assert(currencyExchange1.original == "102.6")
      assert(currencyExchange1.amount == "113.886")
    }
  }
}
