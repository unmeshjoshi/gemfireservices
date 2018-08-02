package com.gemfire.models

case class AggregatedPosition(positions: Seq[DerivedPosition]) {
  def balance: BigInt = positions.map(p => BigInt(p.value)).sum
}
