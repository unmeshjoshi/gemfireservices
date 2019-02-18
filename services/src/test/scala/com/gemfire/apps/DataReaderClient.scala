package com.gemfire.apps

import java.util

import com.gemfire.models.Position
import com.gemfire.repository.{ClientCacheProvider, PositionCache}

import scala.collection.JavaConversions._
object DataReaderClient extends App {
  val positionCache = new PositionCache(ClientCacheProvider.clientCache)
  private val region = positionCache.positionRegion
  private val position1: Position = positionCache.get("5_2018-01-28_316335490")

  println(position1)
}
