package com.gemfire.test.eventapp

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.gemfire.models.{Position, PositionType}
import com.gemfire.repository.ClientCacheProvider
import org.apache.geode.cache.{GemFireCache, Region}

import scala.concurrent.duration._

object Publisher extends  App {
  implicit val system = ActorSystem("publisher")
  implicit val matelializer = ActorMaterializer()
  private val cache: GemFireCache = ClientCacheProvider.create
  private val region: Region[AnyRef, Position] = cache.getRegion("Positions")
  Source.tick(1 second, 1 second, 1).map(p ⇒ {
    val newPosition = new Position(1, PositionType.SAVING, "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28")
    region.put(newPosition.key(), newPosition)
    p
  }).to(Sink.ignore).run()
}
