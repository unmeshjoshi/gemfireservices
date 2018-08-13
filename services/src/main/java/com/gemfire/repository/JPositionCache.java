package com.gemfire.repository;

import com.gemfire.functions.GetValuatedPositions;
import com.gemfire.functions.Multiply;
import com.gemfire.models.Position;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;

import java.util.List;

public class JPositionCache {

    private final Region<String, Position> reg;
    private ClientCache clientCache;

    public JPositionCache(ClientCache clientCache) {
        reg = clientCache.getRegion("Positions");
        this.clientCache = clientCache;
    }

    public int multiplyOnServer(int x, int y) {
        Multiply function = new Multiply();
        Execution execution = FunctionService.onRegion(reg).withArgs(new Object[]{x, y});
        ResultCollector result = execution.execute(function);
        return (Integer)((List)result.getResult()).get(0);
    }


    public void getPositionsWithGemfireFunction() {
        Execution execution = FunctionService.onRegion(reg).withArgs(new Object[]{new Integer[]{100, 20}, "USD"});
        GetValuatedPositions positions = new GetValuatedPositions();
        ResultCollector result = execution.execute(positions);
        System.out.println(((List)result.getResult()).get(0));
    }

}

