package com.gemfire.test

import com.gemfire.repository._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

abstract class FinancialDataFixture extends FunSuite with BeforeAndAfter with Matchers with Eventually {
  val positionCache = new PositionCache(ClientCacheProvider.create)
  val fxRateCache = new FxRatesCache(ClientCacheProvider.create)
  val marketPriceCache: MarketPriceCache = new MarketPriceCache(ClientCacheProvider.create)
  val transactionCache: TransactionCache = new TransactionCache(ClientCacheProvider.create)
  val dataGenerator = new DataGenerator(positionCache, fxRateCache, marketPriceCache, transactionCache)

  before {
    dataGenerator.seedData()
  }

  after {
    dataGenerator.clearData()
  }
}
