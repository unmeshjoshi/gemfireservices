package com.gemfire.functions;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;

public class Multiply implements Function {
    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public void execute(FunctionContext context) {
        RegionFunctionContext rctx = (RegionFunctionContext)context;
        Region<Object, Object> dataSet = rctx.getDataSet();
        Object[] args = (Object[]) context.getArguments();
        Integer first = (Integer) args[0];
        Integer second = (Integer) args[1];



        rctx.getResultSender().lastResult(first * second);
    }

    @Override
    public String getId() {
        return "MULT";
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
