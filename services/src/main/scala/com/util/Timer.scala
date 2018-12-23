package com.util

import java.util.concurrent.TimeUnit

object Timer {
  def time(callback: () => Unit) {
    val startTime = System.nanoTime()
    callback()
    val endTime = System.nanoTime()
    val timeTaken = TimeUnit.NANOSECONDS.toMillis((endTime - startTime))
    println(s"${timeTaken} millis in ${Thread.currentThread().getName}")
  }

  def timeWithResult[T](callback: () => T):T = {
    val startTime = System.nanoTime()
    val result = callback()
    val endTime = System.nanoTime()
    val timeTaken = TimeUnit.NANOSECONDS.toMillis((endTime - startTime))
    println(s"${timeTaken} millis in ${Thread.currentThread().getName}")
    result
  }
}