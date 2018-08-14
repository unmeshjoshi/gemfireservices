package com.gemfire.test

import com.gemfire.repository._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

abstract class FinancialCacheTest extends FunSuite with BeforeAndAfter with Matchers with Eventually {

  val positionCache = new PositionCache(ClientCacheProvider.clientCache)
  val fxRateCache = new FxRatesCache(ClientCacheProvider.clientCache)
  val marketPriceCache: MarketPriceCache = new MarketPriceCache(ClientCacheProvider.clientCache)
  val dataGenerator = new DataGenerator(positionCache, fxRateCache, marketPriceCache)

  before {
    dataGenerator.seedData()
  }

  after {
    dataGenerator.clearData()
  }
}