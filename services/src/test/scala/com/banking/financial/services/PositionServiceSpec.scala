package com.banking.financial.services

import com.gemfire.test.FinancialCacheTest

class PositionServiceTest extends FinancialCacheTest {
  test("should get paginated positions for given parameters") {
    val positionService = new PositionService(positionCache)
    val response = positionService.getPositions(PositionRequest())
    assert(response.elements.size == 2)
    assert(response.aggregation.balance == BigDecimal("3120514160"))
  }
}
