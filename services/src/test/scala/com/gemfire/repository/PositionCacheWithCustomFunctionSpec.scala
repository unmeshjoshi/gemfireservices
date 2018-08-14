package com.gemfire.repository

import com.gemfire.test.FinancialCacheTest
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.scalatest.concurrent.Eventually

class PositionCacheWithCustomFunctionSpec extends FunSuite with Matchers with Eventually with BeforeAndAfter {

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

  test("should get valuated positions with custom gemfire function") {

  }
}
