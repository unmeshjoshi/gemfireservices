package com.gemfire.repository

import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.util.Properties

import com.gemfire.authorization.OnlyFunctionCallsSecurityManager
import com.gemfire.loader.VisibilityLoader
import com.gemfire.models.Position
import org.apache.geode.DataSerializer
import org.apache.geode.cache.{CacheFactory, RegionShortcut}
import org.apache.geode.distributed.ConfigurationProperties.MCAST_PORT
import org.apache.geode.pdx.ReflectionBasedAutoSerializer
import org.apache.geode.pdx.internal.{PdxWriterImpl, TypeRegistry}
import org.scalatest.FunSuite

class SerializationSpec extends FunSuite {

  test("measure serialialization size") {
    createCache

    TypeRegistry.setPdxSerializer(new ReflectionBasedAutoSerializer("com.gemfire.models.*"))
    val position = new Position(2, "SAVING", "9952388706", "EQUITY", "CASH_EQUIVALANT", "92824", 4879, "444", new java.math.BigDecimal(130134482), "INR", "2018-01-28")
    val stream = new ByteArrayOutputStream()
    DataSerializer.writeObject(position, new DataOutputStream(stream), false)
    println(stream.toByteArray.size)
  }



  private def createCache = {
    val props = new Properties()
    props.setProperty(MCAST_PORT, "0")
    props.setProperty("security-manager", "com.gemfire.authorization.OnlyFunctionCallsSecurityManager")

    val factory = new CacheFactory(props)
    val cache = factory
      .setSecurityManager(new OnlyFunctionCallsSecurityManager())
//      .setPdxSerializer(new ReflectionBasedAutoSerializer("com.*"))
      .create()

    cache.createRegionFactory(RegionShortcut.PARTITION).create("Positions")
    cache.createRegionFactory(RegionShortcut.REPLICATE).create("FxRates")
    cache.createRegionFactory(RegionShortcut.REPLICATE).create("MarketPrices")
    cache.createRegionFactory(RegionShortcut.PARTITION).setCacheLoader(new VisibilityLoader()).create("Visibility")
    cache
  }

}
