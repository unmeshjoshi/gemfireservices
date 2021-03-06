package com.gemfire.repository

import com.banking.financial.services.PositionRequest
import com.gemfire.models.{DerivedPosition, Position}
import com.gemfire.test.FinancialDataFixture

class PositionCacheSpec extends FinancialDataFixture {
  test("should get positions for multiple account keys") {
    val positions: java.util.List[Position] = positionCache.getPositionsForDate(List(1, 2), "EQUITY", date = "2018-01-28")
    assert(positions.size == 8)
  }

  test("should get position for given date") {
    val positions: java.util.List[Position] = positionCache.getPositionsForDate(1.toString, "2018-01-28")
    assert(6 == positions.size)
  }

  test("should get position for assetClass and date") {
    val positions: java.util.List[Position] = positionCache.getPositionsForAssetClass(1.toString, "EQUITY", "2018-01-28")
    assert(4 == positions.size)
  }

  test("should multiply positions with FX rates") {
    val request = PositionRequest(List(1), "EQUITY", "USD", "2018-01-28")
    val positions: Seq[DerivedPosition] = positionCache.getPositionsForAssetClass(request)
    assert(4 == positions.size)
  }

  test("should fail invoking custom function from OQL") {
    assertThrows[org.apache.geode.cache.client.ServerOperationException] {
      positionCache.getPositionsForAssetClassWithFxConversion(1.toString, "EQUITY", "2018-01-28", "USD")
    }
  }
}
