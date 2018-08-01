package com.gemfire.repository

import com.gemfire.connection.GemfireResository
import com.gemfire.model.Position
import org.apache.geode.cache.Region
import org.apache.geode.cache.query.{QueryService, SelectResults}

class PositionCache extends GemfireResository {
  def getPositionsForAssetClass(acctKey: String, assetClass:String, date: String, currency:String):java.util.List[Position] = {
    val query = queryService.newQuery("select p, fx.fxRate from /Positions p, /FxRates fx where p.accountKey = $1 and p.positionDate = $2 and p.assetClassL1 = $3 and p.currency = fx.fromCurrency and fx.toCurrency = $4")
    val a = Array(new Integer(acctKey), date, assetClass, currency)
    val result = query.execute(a.asInstanceOf[Array[Object]])
    result.asInstanceOf[SelectResults[Position]].asList()
  }

  def getPositionsForAssetClass(acctKey: String, assetClass:String, date: String):java.util.List[Position] = {
    val query = queryService.newQuery("select * from /Positions p where p.accountKey = $1 and p.positionDate = $2 and p.assetClassL1 = $3")
    val a = Array(new Integer(acctKey), date, assetClass)
    val result = query.execute(a.asInstanceOf[Array[Object]])
    result.asInstanceOf[SelectResults[Position]].asList()
  }

  def getPositionsForDate(acctKey: String, date: String):java.util.List[Position] = {
    val query = queryService.newQuery("select * from /Positions p where p.accountKey = $1 and p.positionDate = $2")
    val a = Array(new Integer(acctKey), date)
    val result = query.execute(a.asInstanceOf[Array[Object]])
    result.asInstanceOf[SelectResults[Position]].asList()
  }


  val reg: Region[String, Position] = clientCache.getRegion("Positions")


  private val queryService: QueryService = clientCache.getQueryService()

  def groupByAccountType(acctKey:Int) = {
    val query = queryService.newQuery("select accountType, sum(p.balance) from /Positions p group by accountType")
    query.execute().asInstanceOf[SelectResults[Map[String, Double]]]
  }

  def add = (position: Position) => {
    reg.put(position.key(), position)
  }

  def get = (id: String) => reg.get(id)

  //test helper to clear cache
  override def clear(): Unit = {
    reg.clear()
  }
}