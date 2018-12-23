package com.banking.financial.services.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.gemfire.repository.{ClientCacheProvider, PositionCache}

import scala.concurrent.{ExecutionContextExecutor, Future}


object HttpService extends App {
  implicit val actorSystem = ActorSystem("GemfireService")
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val cache = ClientCacheProvider.clientCache

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case request@HttpRequest(GET, Uri.Path("/get-cached-values"), _, _, _) =>
      implicit val blockingDispatcher = actorSystem.dispatchers.lookup("service-blocking-dispatcher")
      httpOk(Future { new PositionCache(cache).getPositionsWithGemfireFunction().toString() })
  }

  private def httpOk(responseF: Future[String]) = {
    responseF.map(response ⇒ {
      HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, ByteString.fromString(response)), status = StatusCodes.OK)
    })
  }

  Http().bindAndHandleAsync(requestHandler, "127.0.0.1", 9099)

}
