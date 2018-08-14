package com.banking.financial.services

import com.gemfire.models.{AggregatedPosition, DerivedPosition}
import com.gemfire.repository.PositionCache

case class PositionRequest(val accountKeys: List[Int] = List(1, 2),
                           val assetClass: String = "CASH",
                           val reportingCurrency: String = "INR",
                           val date: String = "2018-01-28",
                           val aggregate: String = "AMOUNT",
                           val aggregate1: String = "GAIN_LOSS", //possibly a list?
                           val sortBy: String = "AMOUNT",
                           val sortOrder: String = "DESC", //enum?
                           val pageSize: Int = 10,
                           val page: Int = 2
                          )
/*
/positions?
&assetClass=CASH
&reportingCurrency=INR
&date=20-Jun-2018
&aggregate=AMOUNT
&aggregate=GAIN_LOSS
&sortBy=AMOUNT
&sortOrder=DESC
&pageSize=20
&page=2
&includeData=true
 */

class PositionService(positionCache: PositionCache) {

  def getPositions(positionRequest:PositionRequest): PositionResponse = {
    val positions: Seq[DerivedPosition] = positionCache.getPositionsForAssetClass(positionRequest)
    val sortedPositions = positions.sortBy(_.value)
    PositionResponse(sortedPositions.map(_.position).toList.take(positionRequest.pageSize), Aggregation(AggregatedPosition(positions).balance, 0), Page(positions.size, 10, 2))
  }
}
