package com.gemfire.functions

import java.util

import com.gemfire.models.{FxRate, MarketPrice, Position, ValuatedPosition}
import org.apache.geode.cache.execute.{Function, FunctionContext, RegionFunctionContext}
import org.apache.geode.cache.partition.PartitionRegionHelper
import org.apache.geode.cache.query.SelectResults
import org.apache.geode.cache.{Cache, CacheFactory, Region}

import scala.collection.JavaConverters._

class GetValuatedPositionsScala extends Function {
  override def hasResult: Boolean = true


  override def execute(context: FunctionContext): Unit = {
    val rctx: RegionFunctionContext = context.asInstanceOf[RegionFunctionContext]
    val positionRegion: Region[AnyRef, Position] = rctx.getDataSet[AnyRef, Position]
    val args: Args = context.getArguments.asInstanceOf[Args]
    val aggregateBy = args.aggregateBy

    val valuatedPositions = calculatePositions(rctx, positionRegion, args.acctKeys, args.reportingCurrency)
    val sender = rctx.getResultSender[java.util.List[ValuatedPosition]]
    val stringToPositions = valuatedPositions.asScala.groupBy(p ⇒ {
      p.getPosition.getAssetClassL1
    })

    sender.lastResult(valuatedPositions)
  }

  private def calculatePositions(rctx: RegionFunctionContext, positionRegion: Region[AnyRef, Position], acctKeys: Array[Int], reportingCurrency: String): util.List[ValuatedPosition] = {
    val cache: Cache = CacheFactory.getAnyInstance
    val fxRates: Region[AnyRef, FxRate] = cache.getRegion("/FxRates")
    val marketPrices: Region[AnyRef, MarketPrice] = cache.getRegion("/MarketPrices")
    try {
      val positionResult: util.List[ValuatedPosition] = new util.ArrayList[ValuatedPosition]
      acctKeys.foreach(acctKey ⇒ {
        val calculateMarketValue = {
          val localDataForContext: Region[AnyRef, AnyRef] = PartitionRegionHelper.getLocalDataForContext(rctx)
          val result: SelectResults[Position] = localDataForContext.query("accountKey = " + acctKey)
          val serializedPositions: util.List[Position] = result.asList //TODO: Remove OQL access
          if (Option(serializedPositions).isDefined)
            valuatePosition(reportingCurrency, fxRates, marketPrices, positionResult, serializedPositions)
        }
      })
      positionResult
    } catch {
      case e: Exception ⇒
        throw new RuntimeException(e)

    }
  }

  private def valuatePosition(reportingCurrency: String, fxRates: Region[AnyRef, FxRate], marketPrices: Region[AnyRef, MarketPrice], positionResult: util.List[ValuatedPosition], positions: util.List[Position]): Unit = {
    import scala.collection.JavaConversions._
    positions.map(position ⇒ {
      val positionCurrency: String = position.getCurrency
      val exchangeRate: FxRate = fxRates.get(FxRate.keyFrom(positionCurrency, reportingCurrency))
      val securityId: String = position.getSecurityId
      val marketPrice: MarketPrice = marketPrices.get(securityId)
      positionResult.add(new ValuatedPosition(position, exchangeRate, marketPrice))
    })
  }

  override def getId: String = "GetValuedPositions"

  override def optimizeForWrite: Boolean = false

  override def isHA: Boolean = false
}
