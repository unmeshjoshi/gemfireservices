package com.gemfire.apps

import java.time.LocalDate
import java.util

import com.gemfire.models.{MarketPrice, Position, PositionType, Transaction}
import com.gemfire.repository._

object DataIngestionClient extends App {
  val positionCache = new PositionCache(ClientCacheProvider.clientCache)
  val transactionCache = new TransactionCache(ClientCacheProvider.clientCache)

  seedPositions
  seedMarketPrices
  seedTransactions

  private def seedPositions = {
    val date = LocalDate.now()
    for (i ← 1 to 1000) {
      val positionDate = date.plusDays(i)
      positionCache.add(new Position(20000 + i, PositionType.SAVING, "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482 + i), "INR", positionDate.toEpochDay + ""))
    }
  }

  private def seedMarketPrices = {
    val marketPriceCache: MarketPriceCache = new MarketPriceCache(ClientCacheProvider.clientCache)
    marketPriceCache.add(new MarketPrice("USD", new java.math.BigDecimal("100"), new java.math.BigDecimal("200"), new java.math.BigDecimal("300"), new java.math.BigDecimal("100")))
  }

  private def seedTransactions = {
    (99999 to 299999).foreach(i ⇒ {
      val entryTuple = newTransactionsEntry(s"995238${i}")
      transactionCache.add(entryTuple._1, entryTuple._2)
    })
  }

  private def newTransactionsEntry(accountNumber: String): (String, util.ArrayList[Transaction]) = {
    val transactionDate = "2018-1-2"
    val transactions = new java.util.ArrayList[Transaction]()
    (1 to 1000).foreach(i ⇒ {
      transactions.add(new Transaction(s"tranId_${i}", transactionDate, "100", "Taxes", accountNumber))
    })
    val key = s"${accountNumber}_${transactionDate}"
    (key, transactions)
  }


}
