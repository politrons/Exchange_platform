package com.politrons.service

import com.politrons.command.ConvertCommand
import com.politrons.dao.ConversionDAO
import com.politrons.view.Conversion
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import zio.{Runtime, ZIO, ZLayer}

import scala.util.Try

class ConversionServiceSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll with MockitoSugar {

  feature("Conversion Service") {

    scenario("Conversion service invoke DAO and return successfully") {
      Given("a ConversionService with CommandConversion and ConversionDAO Mock")
      val service = ConversionService()
      val convertProgram = service.convert()
      When("We evaluate the program passing the dependencies")
      val command = ConvertCommand("GBP", "EUR", 100)
      val mockDao = mock[ConversionDAO]
      when(mockDao.convert("GBP", "EUR", 100)).thenReturn(ZIO.succeed(Conversion("10", "11", "12")))
      val dependencies = ZLayer.succeed(command) ++ ZLayer.succeed(mockDao)
      val conversion = Runtime.global.unsafeRun(convertProgram.provideLayer(dependencies))
      Then("Return a Conversion successfully")
      assert(conversion != null)
      assert(conversion.exchange == "10")
      assert(conversion.amount == "11")
      assert(conversion.original == "12")
    }

    scenario("Conversion service invoke DAO and return error") {
      Given("a ConversionService with CommandConversion and ConversionDAO Mock")
      val service = ConversionService()
      val convertProgram = service.convert()
      When("We evaluate the program passing the dependencies")
      val command = ConvertCommand("GBP", "EUR", 100)
      val mockDao = mock[ConversionDAO]
      when(mockDao.convert("GBP", "EUR", 100)).thenReturn(ZIO.fail(new IllegalArgumentException()))
      val dependencies = ZLayer.succeed(command) ++ ZLayer.succeed(mockDao)
      val conversion = Try(Runtime.global.unsafeRun(convertProgram.provideLayer(dependencies)))
      Then("Return an error")
      assert(conversion.isFailure)

    }
  }
}
