package com.politrons.dao

import com.politrons.mocks.CurrencyExchangeServiceMock
import com.politrons.view.Conversion
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import zio.Runtime

import scala.util.Try

class ConversionDAOSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  override def beforeAll(): Unit ={
    CurrencyExchangeServiceMock.stop()
  }

  feature("Conversion DAO") {

    ignore("Running test against currency_exchange_service successfully") {
      Given("An instance of ConversionDAO and a mock of service [Currency_exchange_service]")
      CurrencyExchangeServiceMock.start(Conversion("10", "11", "12"))
      val dao = ConversionDAO()
      When("I invoke use the connector to make the request")
      val program = dao.convert("GBP", "EUR", 100)
      Then("The program return the Conversion properly")
      val conversion = Runtime.global.unsafeRun(program)
      assert(conversion != null)
      assert(conversion.exchange == "10")
      assert(conversion.amount == "11")
      assert(conversion.original == "12")
      CurrencyExchangeServiceMock.stop()
    }

    scenario("Running test against currency_exchange_service shutdown") {
      Given("An instance of ConversionDAO and a mock of service [Currency_exchange_service] down")
      val dao = ConversionDAO()
      When("I invoke use the connector to make the request")
      val program = dao.convert("GBP", "EUR", 100)
      Then("The program return the Conversion properly")
      val tryConversion = Try(Runtime.global.unsafeRun(program))
      assert(tryConversion.isFailure)
    }
  }
}
