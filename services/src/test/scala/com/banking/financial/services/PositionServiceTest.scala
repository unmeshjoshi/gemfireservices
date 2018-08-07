package com.banking.financial.services

import com.gemfire.models.{FxRate, Position}
import com.gemfire.repository.{ClientCacheProvider, FxRatesCache, PositionCache}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class PositionServiceTest extends FunSuite with BeforeAndAfter with Matchers with Eventually {
  val positionCache = new PositionCache(ClientCacheProvider.clientCache)
  val fxRateCache = new FxRatesCache(ClientCacheProvider.clientCache)

  before {
    seedData()
  }

  after {
    clearData()
  }

  test("should get paginated positions for given parameters") {
    val positionService = new PositionService(positionCache)
    val response = positionService.getPositions(PositionRequest())
    assert(response.elements.size == 2)
    assert(response.aggregation.balance == BigDecimal("3120514160"))
  }


  private def seedData(): Unit = {
    positionCache.add(new Position(1, "SAVING", "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", 130134482, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING_PLUS", "9952388707", "EQUITY_PLUS", "CASH_EQUIVALANT", "92824", 4879, "444", 130134482, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "CURRENT", "8805342674", "EQUITY", "CASH_EQUIVALANT", "77189", 9387, "666", 362750915, "USD", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING", "7076923837", "CASH", "CASH_EQUIVALANT", "40718", 9454, "333", 780128540, "CAD", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING_PLUS", "6334231406", "EQUITY", "CASH_EQUIVALANT", "10120", 2655, "222", 837344728, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "CURRENT", "9928894277", "EQUITY", "INVESTMENT", "26510", 9439, "555", 6710203, "INR", "2018-01-28"))


    positionCache.add(new Position(2, "SAVING", "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", 130134482, "INR", "2018-01-28"))
    positionCache.add(new Position(2, "SAVING_PLUS", "9952388707", "EQUITY_PLUS", "CASH_EQUIVALANT", "92824", 4879, "444", 130134482, "INR", "2018-01-28"))
    positionCache.add(new Position(2, "CURRENT", "8805342674", "EQUITY", "CASH_EQUIVALANT", "77189", 9387, "666", 362750915, "USD", "2018-01-28"))
    positionCache.add(new Position(2, "SAVING", "7076923837", "CASH", "CASH_EQUIVALANT", "40718", 9454, "333", 780128540, "CAD", "2018-01-28"))
    positionCache.add(new Position(2, "SAVING_PLUS", "6334231406", "EQUITY", "CASH_EQUIVALANT", "10120", 2655, "222", 837344728, "INR", "2018-01-28"))
    positionCache.add(new Position(2, "CURRENT", "9928894277", "EQUITY", "INVESTMENT", "26510", 9439, "555", 6710203, "INR", "2018-01-28"))

    fxRateCache.add(new FxRate("USD", "AUS", 2, "2018-01-28"))
    fxRateCache.add(new FxRate("USD", "CAD", 2, "2018-01-28"))
    fxRateCache.add(new FxRate("USD", "INR", 2, "2018-01-28"))
    fxRateCache.add(new FxRate("INR", "USD", 2, "2018-01-28"))
    fxRateCache.add(new FxRate("CAD", "INR", 2, "2018-01-28"))

    fxRateCache.add(new FxRate("USD", "USD", 1, "2018-01-28"))
    fxRateCache.add(new FxRate("INR", "INR", 1, "2018-01-28"))
    fxRateCache.add(new FxRate("AUS", "AUS", 1, "2018-01-28"))
    fxRateCache.add(new FxRate("CAD", "CAD", 1, "2018-01-28"))

  }

  private def clearData(): Unit = {
    positionCache.clear()
    fxRateCache.clear()
  }
}
