package com.gemfire.apps

import com.gemfire.models.{MarketPrice, Position}
import com.gemfire.repository._

object DataIngestionClient extends App {
  val positionCache = new PositionCache(ClientCacheProvider.clientCache)
  positionCache.add(new Position(5, "SAVING", "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28"))

  val marketPriceCache: MarketPriceCache = new MarketPriceCache(ClientCacheProvider.clientCache)
  marketPriceCache.add(new MarketPrice("USD", new java.math.BigDecimal("100"), new java.math.BigDecimal("200"), new java.math.BigDecimal("300"), new java.math.BigDecimal("100")))
}
