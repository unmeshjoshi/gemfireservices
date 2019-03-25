package com.gemfire.repository

import java.util

import com.gemfire.connection.GemfireRepository
import com.gemfire.models.{Position, Transaction}
import org.apache.geode.cache.{GemFireCache, Region}

class TransactionCache(val cache: GemFireCache) extends GemfireRepository {
  def executeOql(oql: String) = {
    val queryService = cache.getQueryService()
    println(s"executing query ${oql}")
    queryService.newQuery(oql).execute()
  }

  def add(key: String, transactions: java.util.ArrayList[Transaction]): Unit = {
    transactionRegion.put(key, transactions)
  }

  val transactionRegion: Region[String, java.util.ArrayList[Transaction]] = cache.getRegion("/Transactions")



  override def clear(): Unit = {
    transactionRegion.clear()
  }
}
