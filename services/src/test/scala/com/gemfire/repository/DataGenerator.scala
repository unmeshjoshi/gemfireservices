package com.gemfire.repository

import com.gemfire.models.{FxRate, Position, PositionType}


class DataGenerator(positionCache:PositionCache, fxRateCache: FxRatesCache, marketPriceCache:MarketPriceCache) {
  def seedData(): Unit = {
    positionCache.add(new Position(1, PositionType.SAVING, "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28"))
    positionCache.add(new Position(1, PositionType.SAVING, "9952388707", "EQUITY_PLUS", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28"))
    positionCache.add(new Position(1, PositionType.SAVING, "8805342674", "EQUITY", "CASH_EQUIVALANT", "77189", 9387, "666", new java.math.BigDecimal(362750915), "USD", "2018-01-28"))
    positionCache.add(new Position(1, PositionType.SAVING, "7076923837", "CASH", "CASH_EQUIVALANT", "40718", 9454, "333", new java.math.BigDecimal(780128540), "CAD", "2018-01-28"))
    positionCache.add(new Position(1, PositionType.SAVING, "6334231406", "EQUITY", "CASH_EQUIVALANT", "10120", 2655, "222", new java.math.BigDecimal(837344728), "INR", "2018-01-28"))
    positionCache.add(new Position(1, PositionType.SAVING, "9928894277", "EQUITY", "INVESTMENT", "26510", 9439, "555", new java.math.BigDecimal(6710203), "INR", "2018-01-28"))


    positionCache.add(new Position(2, PositionType.SAVING, "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28"))
    positionCache.add(new Position(2, PositionType.SAVING, "9952388707", "EQUITY_PLUS", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28"))
    positionCache.add(new Position(2, PositionType.SAVING, "8805342674", "EQUITY", "CASH_EQUIVALANT", "77189", 9387, "666", new java.math.BigDecimal(362750915), "USD", "2018-01-28"))
    positionCache.add(new Position(2, PositionType.SAVING, "7076923837", "CASH", "CASH_EQUIVALANT", "40718", 9454, "333", new java.math.BigDecimal(780128540), "CAD", "2018-01-28"))
    positionCache.add(new Position(2, PositionType.SAVING, "6334231406", "EQUITY", "CASH_EQUIVALANT", "10120", 2655, "222", new java.math.BigDecimal(837344728), "INR", "2018-01-28"))
    positionCache.add(new Position(2, PositionType.SAVING, "9928894277", "EQUITY", "INVESTMENT", "26510", 9439, "555", new java.math.BigDecimal(6710203), "INR", "2018-01-28"))

    fxRateCache.add(new FxRate("USD", "AUS", new java.math.BigDecimal(2), "2018-01-28"))
    fxRateCache.add(new FxRate("USD", "CAD", new java.math.BigDecimal(2), "2018-01-28"))
    fxRateCache.add(new FxRate("USD", "INR", new java.math.BigDecimal(2), "2018-01-28"))
    fxRateCache.add(new FxRate("INR", "USD", new java.math.BigDecimal(2), "2018-01-28"))
    fxRateCache.add(new FxRate("CAD", "INR", new java.math.BigDecimal(2), "2018-01-28"))

    fxRateCache.add(new FxRate("USD", "USD", new java.math.BigDecimal(1), "2018-01-28"))
    fxRateCache.add(new FxRate("INR", "INR", new java.math.BigDecimal(1), "2018-01-28"))
    fxRateCache.add(new FxRate("AUS", "AUS", new java.math.BigDecimal(1), "2018-01-28"))
    fxRateCache.add(new FxRate("CAD", "CAD", new java.math.BigDecimal(1), "2018-01-28"))

  }

  def clearData(): Unit = {
    positionCache.clear()
    fxRateCache.clear()
    marketPriceCache.clear()
  }
}
