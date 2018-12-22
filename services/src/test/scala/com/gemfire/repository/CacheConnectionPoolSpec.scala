package com.gemfire.repository

import java.util.concurrent.TimeUnit

import com.gemfire.models.ValuatedPosition
import com.gemfire.test.FinancialDataFixture

class CacheConnectionPoolSpec extends FinancialDataFixture {

  test("") {

    val t1 = new Thread() {
      override def run(): Unit = {
        Timer.time(callFunction) //this call takes ~100 ms
        Timer.time(callFunction) // subsequent call takes ~50 ms
      }
    }

    val t2 = new Thread() {
      override def run(): Unit = {
        Timer.time(callFunction) //~this call takes ~100ms
      }
    }

    val t3 = new Thread() {
      override def run(): Unit = {
        Timer.time(callFunction) //~this call takes ~100ms
      }
    }

    t1.start()
    t2.start()
//
    Thread.sleep(500) // wait for some time to get the connections freed..
//
    t3.start()


    Thread.sleep(2000) // wait for 2 seconds. Join doesnt work in tests.

  }

  def callFunction() = {
    val positions: List[ValuatedPosition] = positionCache.getPositionsWithGemfireFunction()
  }
}


object Timer {
  def time(callback: () => Unit) {
    val startTime = System.nanoTime()
    callback()
    val endTime = System.nanoTime()
    val timeTaken = TimeUnit.NANOSECONDS.toMillis((endTime - startTime))
    println(s"${timeTaken} millis in ${Thread.currentThread().getId}")
  }
}
