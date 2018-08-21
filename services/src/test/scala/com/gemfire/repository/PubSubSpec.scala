package com.gemfire.repository

import java.util.Properties

import org.apache.geode.cache.{Cache, CacheFactory, RegionShortcut}
import org.apache.geode.distributed.ConfigurationProperties.MCAST_PORT
import org.apache.geode.pdx.ReflectionBasedAutoSerializer
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class PubSubSpec extends FunSuite with BeforeAndAfter with Matchers  {

  test("should get valuated positions with custom gemfire function") {
    val cache: Cache = createCache
    val positionCache = new PositionCache(cache)
    val fxRateCache = new FxRatesCache(cache)
    val marketPriceCache: MarketPriceCache = new MarketPriceCache(cache)
    val dataGenerator = new DataGenerator(positionCache, fxRateCache, marketPriceCache)
    dataGenerator.seedData()

    val positions = positionCache.getPositionsWithGemfireFunction()

    assert(12 == positions.size)
  }

  private def createCache = {
    val props = new Properties()
    props.setProperty(MCAST_PORT, "0")

    val factory = new CacheFactory(props)
    val cache = factory
      .setPdxSerializer(new ReflectionBasedAutoSerializer("com.gemfire.models.*"))
      .create()
    cache.createRegionFactory(RegionShortcut.PARTITION).create("Positions")
    cache.createRegionFactory(RegionShortcut.REPLICATE).create("FxRates")
    cache.createRegionFactory(RegionShortcut.REPLICATE).create("MarketPrices")
    cache
  }
}
