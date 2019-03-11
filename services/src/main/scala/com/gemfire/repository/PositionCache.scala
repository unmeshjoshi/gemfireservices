package com.gemfire.repository

import java.util
import java.util.concurrent.TimeUnit

import com.banking.financial.services.PositionRequest
import com.gemfire.connection.GemfireRepository
import com.gemfire.functions.{Args, GetValuatedPositions, Multiply}
import com.gemfire.models.{DerivedPosition, FxRate, Position, ValuatedPosition}
import com.util.Timer
import org.apache.geode.cache.execute.{Execution, FunctionService, ResultCollector}
import org.apache.geode.cache.query.{QueryService, SelectResults, Struct}
import org.apache.geode.cache.{GemFireCache, Region}
import org.apache.geode.distributed.DistributedMember
import org.apache.geode.internal.cache.execute.DefaultResultCollector
import org.apache.geode.pdx.PdxInstance

import scala.collection.JavaConverters._

/**
  *
  * FIXME: Position region is partitioned, so all oqls need to have 'distinct' in select criteria. Its not possible to clear region in test as well.
  */
class PositionCache(val
                    cache: GemFireCache) extends GemfireRepository {

  val positionRegion: Region[String, Position] = cache.getRegion("Positions")

  private val queryService: QueryService = cache.getQueryService()

  class CResultCollector extends ResultCollector[List[ValuatedPosition], List[ValuatedPosition]] {
    var result = List[ValuatedPosition]()
    override def getResult: List[ValuatedPosition] = {
        result
    }

    override def getResult(timeout: Long, unit: TimeUnit): List[ValuatedPosition] = {
      result
    }

    override def addResult(memberID: DistributedMember, resultOfSingleExecution: List[ValuatedPosition]): Unit = {
      this.result = resultOfSingleExecution
    }

    override def endResults(): Unit = ???

    override def clearResults(): Unit = ???
  }

  def getPositionsWithGemfireFunction(): List[ValuatedPosition] = {
    Timer.timeWithResult(() ⇒ {
      val accountKeys = Array[Int](1, 2)
      val reportingCurrency = "INR"

      val args = new Args(accountKeys, reportingCurrency, "agg")
      val execution: Execution[Args,List[ValuatedPosition], List[ValuatedPosition]] =
        FunctionService
          .onRegion(positionRegion).asInstanceOf[Execution[Args, List[ValuatedPosition], List[ValuatedPosition]]]
      execution.setArguments(args).withCollector(new CResultCollector())

      val positions = new GetValuatedPositions
      val result = execution.execute(positions.getId).getResult.asInstanceOf[util.List[_]]
      if (result.size() > 0)
        result.get(0).asInstanceOf[java.util.List[ValuatedPosition]].asScala.toList
      else
        List()
    })
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
    val query = queryService.newQuery("select entry.value from /Positions.entries entry where entry.value.accountKey = $1 and entry.value.positionDate = $2 and entry.value.assetClassL1 = $3 limit 1")
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
    println(s"Adding position with key ${position.key()}")
    positionRegion.put(position.key(), position)
  }

  def get = (id: String) => positionRegion.get(id).asInstanceOf[PdxInstance]

  //test helper to clear cache
  override def clear(): Unit = {
    //    reg.clear() //TODO: Unsupported on partitioned region
  }
}