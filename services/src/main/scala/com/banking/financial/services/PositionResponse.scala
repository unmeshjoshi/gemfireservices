package com.banking.financial.services

import com.gemfire.models.Position

case class Aggregation(balance: BigDecimal, gainLoss: BigDecimal)

case class Page(totalElements: Int, totalPages: Int, currentPage:Int)

case class PositionResponse(elements: List[Position], aggregation: Aggregation, page: Page)


