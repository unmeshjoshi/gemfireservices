package com.gemfire.repository

import com.gemfire.connection.GemfireRepository
import com.gemfire.models.MarketPrice
import org.apache.geode.cache.{GemFireCache, Region}
import org.apache.geode.cache.client.ClientCache
import org.apache.geode.cache.query.QueryService

class MarketPriceCache(clientCache: GemFireCache) extends GemfireRepository {

  val reg: Region[String, MarketPrice] = clientCache.getRegion("MarketPrices")

  private val queryService: QueryService = clientCache.getQueryService()

  def add(MarketPrice: MarketPrice): MarketPrice = {
    reg.put(MarketPrice.hashCode().toString, MarketPrice)
  }

  def get = (id: String) => reg.get(id)

  override def clear(): Unit = {
    reg.clear()
  }
}
