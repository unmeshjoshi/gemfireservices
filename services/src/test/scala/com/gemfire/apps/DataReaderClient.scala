package com.gemfire.apps

import com.gemfire.repository.{ClientCacheProvider, PositionCache}
object DataReaderClient extends App {
  val positionCache = new PositionCache(ClientCacheProvider.clientCache)
  private val region = positionCache.positionRegion

  private val oldPosition = positionCache.get("914_2018-01-28_13692003") // 30_2018-01-28_453398853

  positionCache.get("890_2018-01-28_1392570698") // 30_2018-01-28_453398853

  positionCache.get("891_2018-01-28_2008746677") // 30_2018-01-28_453398853
  Thread.sleep(10000)
  println(oldPosition)
}
