package com.banking.financial.services

import com.gemfire.repository.PositionCache

case class PositionQuery(acctKey:String, date:String, assetClass:String, aggregateBy:String, sortBy:String) {

}

class PositionService(positionCache:PositionCache) {


  def getPositions() = {

  }
}
