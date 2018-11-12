package com.gemfire.repository

import com.gemfire.connection.GemfireRepository
import org.apache.geode.cache.GemFireCache

abstract class AssociatedDataCache(cache: GemFireCache) extends GemfireRepository {

}
