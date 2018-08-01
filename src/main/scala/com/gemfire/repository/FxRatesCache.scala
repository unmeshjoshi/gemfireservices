package com.gemfire.repository

import com.gemfire.connection.GemfireResository
import com.gemfire.model.{FxRate, Position}
import org.apache.geode.cache.Region
import org.apache.geode.cache.query.internal.ResultsBag
import org.apache.geode.cache.query.{QueryService, SelectResults}

class FxRatesCache extends GemfireResository {
  def getFxRates(fromCurrency: String, toCurrency:String, date: String):java.util.List[Position] = {
    val query = queryService.newQuery("select * from /FxRates fr where fr.fromCache = $1 and fr.toCache = $2 and fr.date = $3")
    val a: Array[AnyRef] = Array(fromCurrency, toCurrency, date)
    val result = query.execute(a)

    val bag = result.asInstanceOf[ResultsBag]
    new java.util.ArrayList[Position]()
  }


  val reg: Region[String, FxRate] = clientCache.getRegion("FxRates")

  private val queryService: QueryService = clientCache.getQueryService()

  def add(fxRate:FxRate) = {
    reg.put(fxRate.hashCode().toString, fxRate)
  }

  def get = (id: String) => reg.get(id)

  override def clear(): Unit = {
    reg.clear()
  }
}