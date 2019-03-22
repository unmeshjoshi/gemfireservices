package com.gemfire.repository

import com.gemfire.models.{FxRate, Position, PositionType, Transaction}


class DataGenerator(positionCache: PositionCache, fxRateCache: FxRatesCache, marketPriceCache: MarketPriceCache, transactionCache: TransactionCache) {
  def seedData(): Unit = {
    seedPositions
    seedFxRates
    seedTransactions()
  }


  def seedTransactions(): Unit = {
    (1 to 1000).foreach(i ⇒ {
      val entryTuple = newTransactionsEntry(s"995238870${i}")
      transactionCache.add(entryTuple._1, entryTuple._2)
    })
  }

  private def newTransactionsEntry(accountNumber: String): (String, java.util.ArrayList[Transaction]) = {
    val transactionDate = "2018-1-2"
    val transactions = new java.util.ArrayList[Transaction]()
    (1 to 1000).foreach(i ⇒ {
      transactions.add(new Transaction(s"tranId_${i}", transactionDate, "100", "Taxes", accountNumber))
    })
    val key = s"${accountNumber}_${transactionDate}"
    (key, transactions)
  }

  private def seedFxRates() = {
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

  private def seedPositions() = {
    for (i ← 1 to 100000) {
      positionCache.add(new Position(i, PositionType.SAVING, "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28"))
      positionCache.add(new Position(i, PositionType.SAVING, "9952388707", "EQUITY_PLUS", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28"))
      positionCache.add(new Position(i, PositionType.SAVING, "8805342674", "EQUITY", "CASH_EQUIVALANT", "77189", 9387, "666", new java.math.BigDecimal(362750915), "USD", "2018-01-28"))
      positionCache.add(new Position(i, PositionType.SAVING, "7076923837", "CASH", "CASH_EQUIVALANT", "40718", 9454, "333", new java.math.BigDecimal(780128540), "CAD", "2018-01-28"))
      positionCache.add(new Position(i, PositionType.SAVING, "6334231406", "EQUITY", "CASH_EQUIVALANT", "10120", 2655, "222", new java.math.BigDecimal(837344728), "INR", "2018-01-28"))
      positionCache.add(new Position(i, PositionType.SAVING, "9928894277", "EQUITY", "INVESTMENT", "26510", 9439, "555", new java.math.BigDecimal(6710203), "INR", "2018-01-28"))


      //      positionCache.add(new Position(2, PositionType.SAVING, "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28"))
      //      positionCache.add(new Position(2, PositionType.SAVING, "9952388707", "EQUITY_PLUS", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28"))
      //      positionCache.add(new Position(2, PositionType.SAVING, "8805342674", "EQUITY", "CASH_EQUIVALANT", "77189", 9387, "666", new java.math.BigDecimal(362750915), "USD", "2018-01-28"))
      //      positionCache.add(new Position(2, PositionType.SAVING, "7076923837", "CASH", "CASH_EQUIVALANT", "40718", 9454, "333", new java.math.BigDecimal(780128540), "CAD", "2018-01-28"))
      //      positionCache.add(new Position(2, PositionType.SAVING, "6334231406", "EQUITY", "CASH_EQUIVALANT", "10120", 2655, "222", new java.math.BigDecimal(837344728), "INR", "2018-01-28"))
      //      positionCache.add(new Position(2, PositionType.SAVING, "9928894277", "EQUITY", "INVESTMENT", "26510", 9439, "555", new java.math.BigDecimal(6710203), "INR", "2018-01-28"))
    }
  }

  def clearData(): Unit = {
    positionCache.clear()
    fxRateCache.clear()
    marketPriceCache.clear()
  }
}
