package com.gemfire.apps

import com.gemfire.repository.{ClientCacheProvider, PositionCache}
object DataReaderClient extends App {
  val positionCache = new PositionCache(ClientCacheProvider.clientCache)
  private val region = positionCache.positionRegion

  private val oldPosition = positionCache.get("28397") // 30_2018-01-28_453398853

  positionCache.get("28398") // 30_2018-01-28_453398853

  positionCache.get("28400") // 30_2018-01-28_453398853
  println(oldPosition)
}
