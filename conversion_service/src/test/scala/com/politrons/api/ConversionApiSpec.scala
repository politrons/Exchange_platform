package com.politrons.api

import com.google.gson.Gson
import com.politrons.dao.ConversionDAO
import com.politrons.service.ConversionService
import com.politrons.view.Conversion
import com.twitter.finagle.http.{Method, Request}
import com.twitter.util.Await
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import zio.{Runtime, ZIO}
import com.twitter.conversions.DurationOps._

case class ConversionApiSpec() extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll with MockitoSugar {

  private val gson = new Gson()

  feature("Conversion Api") {

    scenario("ConversionAPI service receive a request into the service and return response successfully") {
      Given("A mock for service and dao")
      val mockDao = mock[ConversionDAO]
      val mockService = mock[ConversionService]
      when(mockService.convert()).thenReturn(ZIO.succeed(Conversion("10", "11", "12")))

      val api = ConversionApi(mockService, mockDao)
      When("I invoke the service and run a request")
      val futureResponseProgram = for {
        request <- ZIO.effect {
          val body =
            """{
              |"fromCurrency": "GBP", "toCurrency" : "EUR", "amount" : 102.6
              |}
              |""".stripMargin
          val request = Request(Method.Post, "/api/convert/")
          request.uri("/api/convert/")
          request.setContentString(body)
          request
        }
        service <- api.createService()
        futureResponse <- ZIO.effect(service(request))
        response <- ZIO.effect(Await.result(futureResponse))
        _ <- ZIO.effect(Await.result(service.close(0.seconds)))
      } yield response
      val response = Runtime.global.unsafeRun(futureResponseProgram)
      Then("The response is successful")
      assert(response.statusCode == 200)
      val conversion = gson.fromJson(response.getContentString(), classOf[Conversion])
      assert(conversion != null)
    }


    scenario("ConversionAPI service receive a wrong request into the service and return error") {
      Given("A mock for service and dao")
      val mockDao = mock[ConversionDAO]
      val mockService = mock[ConversionService]
      when(mockService.convert()).thenReturn(ZIO.fail(new IllegalArgumentException()))

      val api = ConversionApi(mockService, mockDao)
      When("I invoke the service and run a request")
      val futureResponseProgram = for {
        request <- ZIO.effect {
          val body =
            """{
              |"fromCurrency": "GBP", "toCurrency" : "EUR", "amount" : 102.6
              |}
              |""".stripMargin
          val request = Request(Method.Post, "/api/convert/")
          request.uri("/api/convert/")
          request.setContentString(body)
          request
        }
        service <- api.createService()
        futureResponse <- ZIO.effect(service(request))
        response <- ZIO.effect(Await.result(futureResponse))
        _ <- ZIO.effect(Await.result(service.close()))
      } yield response
      val response = Runtime.global.unsafeRun(futureResponseProgram)
      Then("The response is wrong")
      assert(response.statusCode == 500)
    }
  }


}
