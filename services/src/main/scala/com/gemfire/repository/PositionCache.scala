package com.gemfire.repository

import com.banking.financial.services.PositionRequest
import com.gemfire.connection.GemfireRepository
import com.gemfire.functions.{GetValuatedPositions, Multiply}
import com.gemfire.models.{DerivedPosition, FxRate, Position}
import org.apache.geode.cache.Region
import org.apache.geode.cache.client.ClientCache
import org.apache.geode.cache.execute.FunctionService
import org.apache.geode.cache.query.{QueryService, SelectResults, Struct}

import scala.collection.JavaConverters._

class PositionCache(clientCache: ClientCache) extends GemfireRepository {
  val reg: Region[String, Position] = clientCache.getRegion("Positions")
  private val queryService: QueryService = clientCache.getQueryService()


  def getPositionsWithGemfireFunction(): Unit = {
    val accountKeys = Array[Integer](100, 20)
    val reportingCurrency = "USD"

    val execution = FunctionService.onRegion(reg).withArgs(Array[AnyRef](accountKeys, reportingCurrency))
    val positions = new GetValuatedPositions
    val result = execution.execute(positions)
    println(result.getResult.asInstanceOf[java.util.List[_]])
  }

  //TODO:This fails. Can not invoke custom functions from OQL
  def getPositionsForAssetClassWithFxConversion(acctKey: String, assetClass: String, date: String, currency: String) = {
    FunctionService.registerFunction(new Multiply())

    val query = queryService.newQuery(
      """select p, fx, MULT(p.balance, fx.fxRate) as valuation
        |from /Positions p, /FxRates fx
        |where p.accountKey = $1
        |and p.positionDate = $2
        |and p.assetClassL1 = $3
        |and fx.forDate = $2
        |and p.currency = fx.fromCurrency
        |and fx.toCurrency = $4
        |order by valuation""".stripMargin)
    val params = Array(new Integer(acctKey), date, assetClass, currency)

    query.execute(params.asInstanceOf[Array[Object]])
      .asInstanceOf[SelectResults[Struct]]
      .asList()
      .asScala
      .map(p => p.get("valuation"))
      .toSeq
  }

  def getPositionsForAssetClass(positionRequest: PositionRequest): Seq[DerivedPosition] = {
    val query = queryService.newQuery(
      """select distinct p, fx
        |from /Positions p, /FxRates fx
        |where p.accountKey in $1
        |and p.positionDate = $2
        |and p.assetClassL1 = $3
        |and fx.forDate = $2
        |and p.currency = fx.fromCurrency
        |and fx.toCurrency = $4""".stripMargin)

    val params = Array(positionRequest.accountKeys.asJava.toArray, positionRequest.date, positionRequest.assetClass, positionRequest.reportingCurrency)

    query.execute(params.asInstanceOf[Array[Object]])
      .asInstanceOf[SelectResults[Struct]]
      .asList()
      .asScala
      .map(p => new DerivedPosition(p.get("p").asInstanceOf[Position], p.get("fx").asInstanceOf[FxRate]))
  }


  def getPositionsForAssetClass(acctKey: String, assetClass: String, date: String): java.util.List[Position] = {
    val query = queryService.newQuery("select distinct * from /Positions p where p.accountKey = $1 and p.positionDate = $2 and p.assetClassL1 = $3")
    val a = Array(new Integer(acctKey), date, assetClass)
    val result = query.execute(a.asInstanceOf[Array[Object]])
    result.asInstanceOf[SelectResults[Position]].asList()
  }

  def getPositionsForDate(acctKeys: List[Int], assetClass: String, date: String): java.util.List[Position] = {
    val query = queryService.newQuery("<TRACE> select distinct * from /Positions p where p.accountKey in $1 and p.assetClassL1 = $2 and p.positionDate = $3")
    val set = acctKeys.asJava.toArray
    val params = Array(set, assetClass, date)
    val result = query.execute(params.asInstanceOf[Array[Object]])
    result.asInstanceOf[SelectResults[Position]].asList()
  }

  def getPositionsForDate(acctKey: String, date: String): java.util.List[Position] = {
    val query = queryService.newQuery("select distinct * from /Positions p where p.accountKey = $1 and p.positionDate = $2")
    val a = Array(new Integer(acctKey), date)
    val result = query.execute(a.asInstanceOf[Array[Object]])
    result.asInstanceOf[SelectResults[Position]].asList()
  }

  def groupByAccountType(acctKey: Int) = {
    val query = queryService.newQuery("select accountType, sum(p.balance) from /Positions p group by accountType")
    query.execute().asInstanceOf[SelectResults[Map[String, Double]]]
  }

  def add = (position: Position) => {
    reg.put(position.key(), position)
  }

  def get = (id: String) => reg.get(id)

  //test helper to clear cache
  override def clear(): Unit = {
//    reg.clear() //TODO: Unsupported on partitioned region
  }
}