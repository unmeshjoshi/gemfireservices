package com.gemfire.apps

import com.gemfire.models.Position
import com.gemfire.repository.{ClientCacheProvider, PositionCache}
object DataReaderClient extends App {
  val positionCache = new PositionCache(ClientCacheProvider.clientCache)
  private val region = positionCache.positionRegion

  private val oldPosition: Position = positionCache.get("20_2018-01-28_453398853") // 30_2018-01-28_453398853

  positionCache.get("30_2018-01-28_453398853") // 30_2018-01-28_453398853

  positionCache.get("31_2018-01-28_453398853") // 30_2018-01-28_453398853

  println(oldPosition)
}
