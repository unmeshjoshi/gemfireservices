package com.gemfire.functions;

import com.gemfire.models.Position;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.query.SelectResults;

import java.util.Collections;
import java.util.List;

public class GetValuatedPositions implements Function {
    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public void execute(FunctionContext context) {
        RegionFunctionContext rctx = (RegionFunctionContext)context;
        Region<Object, Object> dataSet = rctx.getDataSet();
        Object[] args = (Object[]) context.getArguments();
        String assetClass = (String) args[0];
        Integer balanceInUSD = (Integer) args[1];
        String balanceCurrency = (String) args[2];
        List<Position> result = Collections.emptyList();
        try {
            SelectResults<Position> positions = dataSet.query("assetClassL1 = " + assetClass);

            result = positions.asList();
        } catch (Exception e) {
                throw new RuntimeException(e);
        }

        rctx.getResultSender().lastResult(result);
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean optimizeForWrite() {
        return false;
    }

    @Override
    public boolean isHA() {
        return false;
    }
}
