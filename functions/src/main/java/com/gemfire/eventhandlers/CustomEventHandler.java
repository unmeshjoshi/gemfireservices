package com.gemfire.eventhandlers;

import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.util.CacheListenerAdapter;
import org.apache.geode.internal.logging.LogService;

import java.util.Properties;

public class CustomEventHandler extends CacheListenerAdapter implements Declarable {
    public void afterCreate(EntryEvent event) {
        LogService.getLogger().info(event);

    }

    @Override
    public void init(Properties props) {
        LogService.getLogger().info(props);
    }
}

