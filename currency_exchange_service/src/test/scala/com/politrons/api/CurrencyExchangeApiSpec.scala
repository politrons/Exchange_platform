package com.politrons.api

import com.google.gson.Gson
import com.politrons.service.CurrencyExchangeService
import com.politrons.model.CurrencyExchangeRequest
import com.politrons.view.CurrencyExchange
import com.twitter.finagle.http.RequestBuilder
import com.twitter.util.Await
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import zio.{Has, Runtime, URIO}

import java.net.URL

class CurrencyExchangeApiSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  private val gson = new Gson()

  feature("Currency Exchange Api") {

    scenario("Invocation to CurrencyExchangeApi with correct request and correct response from engine ") {
      Given("a mock engine that return success response and a CurrencyExchangeApi service")

      val mockCurrencyExchange = MockCurrencyExchange(URIO.succeed(Right(CurrencyExchange("1.11", "113.886", "102.6"))))

      val api = CurrencyExchangeApi(mockCurrencyExchange)
      When("I invoke the program and pass into the service a Request with proper values")
      val serviceProgram = api.createService()
      val service = Runtime.global.unsafeRun(serviceProgram)
      val request = RequestBuilder()
        .url(new URL(s"http://localhost:9995/api/v1/convert" +
          s"?from=GBR" +
          s"&to=EUR" +
          s"&amount=102.6"))
        .buildGet()
      Then("The service render CurrencyExchange class successfully")
      val response = Await.result(service(request))
      assert(response.statusCode == 200)
      val currencyExchange = gson.fromJson(response.getContentString(), classOf[CurrencyExchange])
      assert(currencyExchange.exchange == "1.11")
      assert(currencyExchange.original == "102.6")
      assert(currencyExchange.amount == "113.886")
    }
  }

  scenario("Invocation to CurrencyExchangeApi with correct request and wrong response from engine ") {
    Given("a mock engine that return success response and a CurrencyExchangeApi service")

    val mockCurrencyExchange = MockCurrencyExchange(URIO.succeed(Left(new IllegalArgumentException())))

    val api = CurrencyExchangeApi(mockCurrencyExchange)
    When("I invoke the program and pass into the service a Request with proper values")
    val serviceProgram = api.createService()
    val service = Runtime.global.unsafeRun(serviceProgram)
    val request = RequestBuilder()
      .url(new URL(s"http://localhost:9995/api/v1/convert" +
        s"?from=GBR" +
        s"&to=EUR" +
        s"&amount=102.6"))
      .buildGet()
    Then("The service render Internal server error.")
    val response = Await.result(service(request))
    assert(response.statusCode == 500)

  }

  /**
   * I found a bug with scalaMock, so to be fast for the challenge and dont get stuck I implement my own mock.
   */
  case class MockCurrencyExchange(mockProgram: URIO[Has[CurrencyExchangeRequest], Either[Throwable, CurrencyExchange]]) extends CurrencyExchangeService {
    override def exchange(): URIO[Has[CurrencyExchangeRequest], Either[Throwable, CurrencyExchange]] = {
      mockProgram
    }
  }
}
