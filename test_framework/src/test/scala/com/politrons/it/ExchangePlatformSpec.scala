package com.politrons.it

import com.google.gson.Gson
import com.politrons.app.{ConversionServer, CurrencyExchangeServer}
import com.politrons.view.Conversion
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.{Await, Future}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FeatureSpec, GivenWhenThen}

import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}

case class ExchangePlatformSpec() extends FeatureSpec with GivenWhenThen with BeforeAndAfterEach with MockitoSugar {

  private implicit val ec: ExecutionContextExecutor = concurrent.ExecutionContext.global
  private val gson = new Gson()

  override def afterEach(): Unit = {
    ConversionServer.stop()
    CurrencyExchangeServer.stop()
  }

  feature("Exchange platform") {

    scenario("Integration test to test the end to end platform happy path") {
      Given("Two servers of the platform up and running and finagle client")
      ScalaFuture {
        ConversionServer.main(Array())
      }
      ScalaFuture {
        CurrencyExchangeServer.main(Array())
      }
      //Waiting the servers to be up and running. Create an endpoint /healthCheck it would be better
      Thread.sleep(5000)

      val client = Http.client
        .newService("localhost:9994")

      When("I invoke the Conversion Server API")

      val body =
        """{
          |"fromCurrency": "GBR", "toCurrency" : "EUR", "amount" : 102.6
          |}
          |""".stripMargin
      val request = Request(Method.Post, "/api/convert/")
      request.uri("/api/convert/")
      request.setContentString(body)

      val futureResponse: Future[Response] = client(request)
      Then("The response from the end to end request is successful")
      val response = Await.result(futureResponse)
      assert(response.statusCode == 200)
      val conversion = gson.fromJson(response.getContentString(), classOf[Conversion])
      assert(conversion != null)
      assert(conversion.exchange == "1.11")
      assert(conversion.amount == "113.886")
      assert(conversion.original == "102.6")
    }


    scenario("Integration test to test the end to end platform ugly path") {
      Given("Two servers of the platform up and running and finagle client")
      ScalaFuture {
        ConversionServer.main(Array())
      }
      ScalaFuture {
        CurrencyExchangeServer.main(Array())
      }
      //Waiting the servers to be up and running. Create an endpoint /healthCheck it would be better
      Thread.sleep(5000)

      val client = Http.client
        .newService("localhost:9994")

      When("I invoke the Conversion Server API with wrong request")

      val body =
        """{
          |"fromCurrency": "Foo", "toCurrency" : "Bla", "amount" : 102.6
          |}
          |""".stripMargin
      val request = Request(Method.Post, "/api/convert/")
      request.uri("/api/convert/")
      request.setContentString(body)

      val futureResponse: Future[Response] = client(request)
      Then("The response from the end to end request is successful")
      val response = Await.result(futureResponse)
      assert(response.statusCode == 500)
    }
  }

}
