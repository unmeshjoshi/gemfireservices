package com.gemfire.models;

import com.gemfire.functions.Multiply;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;

import java.util.Collections;
import java.util.List;

public class JPositionCache {

    private final Region<String, Position> reg;

    public JPositionCache(ClientCache clientCache) {
        reg = clientCache.getRegion("Positions");

    }
    //FIXME introduce query object for all getPosition* methods.
    public List getPositionsForAssetClass() {
        Multiply function = new Multiply();

        Execution execution = FunctionService.onRegion(reg).withArgs(new Object[]{100, 20});
        ResultCollector result = execution.execute(function);
        System.out.println(result.getResult());

        return Collections.EMPTY_LIST;
    }
}

