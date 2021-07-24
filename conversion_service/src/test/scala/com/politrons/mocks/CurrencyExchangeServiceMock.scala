package com.politrons.mocks

import com.google.gson.Gson
import com.politrons.view.Conversion
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, ListeningServer, Service}
import com.twitter.util.{Await, Future, Time}

object CurrencyExchangeServiceMock {

  private val gson = new Gson()
  var server: ListeningServer = _

  def start(conversion: Conversion): Unit = {

    val service = new Service[Request, Response]() {
      def apply(req: Request): Future[Response] = {
        req.path match {
          case "/api/v1/convert" =>
            val response = Response(req.version, Status.Ok)
            response.setContentTypeJson()
            response.setContentString(gson.toJson(conversion))
            Future(response)
        }
      }
    }
    server = Http.server.serve(s"0.0.0.0:9995", service)
  }

  def stop(): Unit = {
    if (server != null)
      Await.result(server.close())
  }

}