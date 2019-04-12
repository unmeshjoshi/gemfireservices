package com.gemfire.repository

import java.util.Properties

import com.gemfire.authorization.OnlyFunctionCallsSecurityManager
import com.gemfire.loader.VisibilityLoader
import com.gemfire.models.Position
import org.apache.geode.cache.{Cache, CacheFactory, RegionShortcut}
import org.apache.geode.distributed.ConfigurationProperties.MCAST_PORT
import org.apache.geode.pdx.ReflectionBasedAutoSerializer
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class EmbeddedGemfireSpec extends FunSuite with BeforeAndAfter with Matchers {

  test("should get position for assetClass and date") {
    val cache: Cache = createCache
    val positionCache = new PositionCache(cache)
    val fxRateCache = new FxRatesCache(cache)
    val marketPriceCache: MarketPriceCache = new MarketPriceCache(cache)
    val transactionCache: TransactionCache = new TransactionCache(cache)

    val dataGenerator = new DataGenerator(positionCache, fxRateCache, marketPriceCache, transactionCache)

    dataGenerator.seedData()
    val positions: java.util.List[Position] = positionCache.getPositionsForAssetClass(1.toString, "EQUITY", "2018-01-28")
    assert(1 == positions.size)

  }

  test("should get valuated positions with custom gemfire function") {
    val cache: Cache = createCache

    val positionCache = new PositionCache(cache)
    val fxRateCache = new FxRatesCache(cache)
    val marketPriceCache: MarketPriceCache = new MarketPriceCache(cache)
    val transactionCache: TransactionCache = new TransactionCache(ClientCacheProvider.clientCache)

    val dataGenerator = new DataGenerator(positionCache, fxRateCache, marketPriceCache, transactionCache)

    dataGenerator.seedData()

    val positions = positionCache.getPositionsWithGemfireFunction()

    assert(12 == positions.size)
  }

  private def createCache = {
    val props = new Properties()
    props.setProperty(MCAST_PORT, "0")
    props.setProperty("security-manager", "com.gemfire.authorization.OnlyFunctionCallsSecurityManager")

    val factory = new CacheFactory(props)
    val cache = factory
      .set("off-heap-memory-size", "200m")
      .setPdxReadSerialized(true)
      .setPdxSerializer(new ReflectionBasedAutoSerializer("com.gemfire.models.*"))
      .setSecurityManager(new OnlyFunctionCallsSecurityManager())
      .setPdxDiskStore("DEFAULT")
      .create()

    cache.createRegionFactory(RegionShortcut.PARTITION).setOffHeap(true).create("Positions")
    cache.createRegionFactory(RegionShortcut.REPLICATE).create("FxRates")
    cache.createRegionFactory(RegionShortcut.REPLICATE).create("MarketPrices")
    cache.createRegionFactory(RegionShortcut.PARTITION).setCacheLoader(new VisibilityLoader()).create("Visibility")
    cache
  }
}
