package com.gemfire.events

import java.util.Properties

import com.gemfire.models.Position
import com.gemfire.repository._
import org.apache.geode.cache.{Cache, CacheFactory, Region, RegionShortcut}
import org.apache.geode.distributed.ConfigurationProperties.MCAST_PORT
import org.apache.geode.pdx.ReflectionBasedAutoSerializer
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.scalatest.concurrent.Eventually

class PubSubSpec extends FunSuite with BeforeAndAfter with Matchers with Eventually {

  test("should be able to get events for particular key") {
      val cache: Cache = createCache

      val region: Region[String, Position] = cache.getRegion("Positions")
      val unit = region.registerInterest("1")

      val positionCache = new PositionCache(cache)
      val fxRateCache = new FxRatesCache(cache)
      val marketPriceCache: MarketPriceCache = new MarketPriceCache(cache)
    val transactionCache: TransactionCache = new TransactionCache(ClientCacheProvider.create)

      val dataGenerator = new DataGenerator(positionCache, fxRateCache, marketPriceCache, transactionCache)

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
