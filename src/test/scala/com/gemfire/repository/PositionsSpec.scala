package com.gemfire.repository

import com.gemfire.model.{FxRate, Position}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class PositionCacheTest extends FunSuite with BeforeAndAfter with Matchers with Eventually {
  val positionCache = new PositionCache()
//
//  after {
//    positionCache.clear()
//  }

  test("should get position for given date") {

    positionCache.add(new Position(1, "SAVING", "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", 130134482, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING_PLUS", "9952388707", "EQUITY_PLUS", "CASH_EQUIVALANT", "92824", 4879, "444", 130134482, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "CURRENT", "8805342674", "EQUITY", "CASH_EQUIVALANT", "77189", 9387, "666", 362750915, "USD", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING", "7076923837", "CASH", "CASH_EQUIVALANT", "40718", 9454, "333", 780128540, "CAD", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING_PLUS", "6334231406", "EQUITY", "CASH_EQUIVALANT", "10120", 2655, "222", 837344728, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "CURRENT", "9928894277", "EQUITY", "INVESTMENT", "26510", 9439, "555", 6710203, "INR", "2018-01-28"))

    val positions: java.util.List[Position] = positionCache.getPositionsForDate(1.toString, "2018-01-28")

    assert(6 == positions.size)

  }

  test("should get position for assetClass and date") {

    positionCache.add(new Position(1, "SAVING", "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", 130134482, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING_PLUS", "9952388707", "EQUITY_PLUS", "CASH_EQUIVALANT", "92824", 4879, "444", 130134482, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "CURRENT", "8805342674", "EQUITY", "CASH_EQUIVALANT", "77189", 9387, "666", 362750915, "USD", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING", "7076923837", "CASH", "CASH_EQUIVALANT", "40718", 9454, "333", 780128540, "CAD", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING_PLUS", "6334231406", "EQUITY", "CASH_EQUIVALANT", "10120", 2655, "222", 837344728, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "CURRENT", "9928894277", "EQUITY", "INVESTMENT", "26510", 9439, "555", 6710203, "INR", "2018-01-28"))

    val positions: java.util.List[Position] = positionCache.getPositionsForAssetClass(1.toString, "EQUITY", "2018-01-28")

    assert(4 == positions.size)
  }

  test("should get should multiply positions with fxrates") {

    positionCache.add(new Position(1, "SAVING", "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", 130134482, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING_PLUS", "9952388707", "EQUITY_PLUS", "CASH_EQUIVALANT", "92824", 4879, "444", 130134482, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "CURRENT", "8805342674", "EQUITY", "CASH_EQUIVALANT", "77189", 9387, "666", 362750915, "USD", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING", "7076923837", "CASH", "CASH_EQUIVALANT", "40718", 9454, "333", 780128540, "CAD", "2018-01-28"))
    positionCache.add(new Position(1, "SAVING_PLUS", "6334231406", "EQUITY", "CASH_EQUIVALANT", "10120", 2655, "222", 837344728, "INR", "2018-01-28"))
    positionCache.add(new Position(1, "CURRENT", "9928894277", "EQUITY", "INVESTMENT", "26510", 9439, "555", 6710203, "INR", "2018-01-28"))


    val fxRateCache = new FxRatesCache()
    fxRateCache.add(new FxRate("USD", "AUS", 2, "2018-07-30"))
    fxRateCache.add(new FxRate("USD", "INR", 2, "2018-07-30"))
    fxRateCache.add(new FxRate("INR", "USD", 2, "2018-07-30"))

    val positions: java.util.List[Position] = positionCache.getPositionsForAssetClass(1.toString, "EQUITY", "2018-01-28", "USD")

    assert(4 == positions.size)

  }

  /**
    *
    */

}
