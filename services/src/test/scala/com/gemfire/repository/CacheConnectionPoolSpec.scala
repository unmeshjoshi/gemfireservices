package com.gemfire.repository

import com.gemfire.models.ValuatedPosition
import com.gemfire.test.FinancialDataFixture
import com.util.Timer

class CacheConnectionPoolSpec extends FinancialDataFixture {

  test("Call gemfire function") {
    println("data ingested")
    callFunction()
//    val t1 = new Thread("first") {
//      override def run(): Unit = {
//        Timer.time(callFunction) //this call takes ~100 ms
//        Timer.time(callFunction) // subsequent call takes ~50 ms
//      }
//    }
//
//    val t2 = new Thread("second") {
//      override def run(): Unit = {
//        Timer.time(callFunction) //~this call takes ~100ms
//      }
//    }
//
//    Thread.sleep(500) // wait for some time to get the connections freed..
//
//
//    Timer.time(callFunction) //~this call takes ~100ms
//    Timer.time(callFunction) //~this call takes ~50ms
//
////    Thread.sleep(500) // wait for some time to get the connections freed..
//
//
//    t1.start()
//    t2.start()
//
//
//
//  Thread.sleep(2000) // wait for 2 seconds. Join doesnt work in tests.

  }

  def callFunction() = {
    val positions: List[ValuatedPosition] = positionCache.getPositionsWithGemfireFunction()
  }
}

