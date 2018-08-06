package com.gemfire.repository

import com.gemfire.models.{DerivedPosition, FxRate, JPositionCache, Position}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class PositionCacheSpec extends FunSuite with BeforeAndAfter with Matchers with Eventually {

  val clientCacheProvider = new ClientCacheProvider()
  val positionCache = new PositionCache(clientCacheProvider.clientCache)
  val fxRateCache = new FxRatesCache(clientCacheProvider.clientCache)

  before {
    seedData()
  }

  after {
    clearData()
  }

  test("should get positions for multiple account keys") {
    val positions: java.util.List[Position] = positionCache.getPositionsForDate(List(1, 2), "2018-01-28")
    assert(positions.size == 12)
  }

  test("should get position for given date") {
    val positions: java.util.List[Position] = positionCache.getPositionsForDate(1.toString, "2018-01-28")
    assert(6 == positions.size)
  }

  test("should get position for assetClass and date") {
    val positions: java.util.List[Position] = positionCache.getPositionsForAssetClass(1.toString, "EQUITY", "2018-01-28")
    assert(4 == positions.size)
  }

  test("should multiply positions with FX rates") {
    val positions: Seq[DerivedPosition] = positionCache.getPositionsForAssetClass(1.toString, "EQUITY", "2018-01-28", "USD")
    assert(4 == positions.size)
  }

  test("should aggregate on balance after FX conversion") {
    // /positions?assetClass=EQUITY&reportingCurrency=INR&date=2018-01-28&aggregate=AMOUNT
    val aggregate = positionCache.getPositionsForAssetClassWithFxConversion(1.toString, "EQUITY", "2018-01-28", "USD")
    val expectedBalance = BigInt("2311129741")
    println(aggregate)
  }

  test("should call custom function with gemfire") {
    val result = new JPositionCache(clientCacheProvider.clientCache).executeMultiplyOnGemfireServer(10, 2)
    assert(result == 20)
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
    fxRateCache.add(new FxRate("USD", "INR", 2, "2018-01-28"))
    fxRateCache.add(new FxRate("INR", "USD", 2, "2018-01-28"))

    fxRateCache.add(new FxRate("USD", "USD", 1, "2018-01-28"))
    fxRateCache.add(new FxRate("INR", "INR", 1, "2018-01-28"))
    fxRateCache.add(new FxRate("AUS", "AUS", 1, "2018-01-28"))
  }

  private def clearData(): Unit = {
    positionCache.clear()
    fxRateCache.clear()
  }
}