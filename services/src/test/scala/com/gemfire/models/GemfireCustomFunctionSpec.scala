package com.gemfire.models

import java.util.concurrent.{ArrayBlockingQueue, ThreadPoolExecutor, TimeUnit}

import com.gemfire.repository.{ClientCacheProvider, JPositionCache}
import org.scalatest.FunSuite

class GemfireCustomFunctionSpec extends FunSuite {
  test("should call custom function with gemfire") {
    import java.util.concurrent.Executors
    val executor = Executors.newFixedThreadPool(100)
    for (i ← 1 to 10000) {
      executor.execute(new Runnable {
        override def run(): Unit = {
          val result = new JPositionCache(ClientCacheProvider.create).multiplyOnServer(10, 2)
          assert(result == 20)
        }
      }
      )
      Thread.sleep(100) //wait for one second
    }
  }
}
