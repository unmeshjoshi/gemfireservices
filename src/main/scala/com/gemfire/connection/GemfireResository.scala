package com.gemfire.connection

import org.apache.geode.cache.client.{ClientCache, ClientCacheFactory, ClientRegionFactory, ClientRegionShortcut}

trait GemfireResository {
  val clientCache: ClientCache = new ClientCacheFactory().set("cache-xml-file", "xml/cache-xml-file.xml")
                          .create()


  def clear()

}
