package com.gemfire.loader;

import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.LoaderHelper;

import java.util.ArrayList;
import java.util.List;

public class VisibilityLoader implements CacheLoader {
    @Override
    public Object load(LoaderHelper helper) throws CacheLoaderException {
        String memberId = (String) helper.getKey();
        VisibilityArgs additionalArguments = (VisibilityArgs) helper.getArgument();

        //make service call to demographic service to get accountKeys
        return getAccountKeysFor(memberId);
    }

    private Object getAccountKeysFor(String memberId) {
        List<String> accountKeys = new ArrayList<>();
        accountKeys.add("1");
        accountKeys.add("2");
        return accountKeys;
    }

    @Override
    public void close() {

    }
}
