package com.gemfire.functions;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;

public class Multiply implements Function {
    @Override
    public boolean hasResult() {
        return false;
    }

    @Override
    public void execute(FunctionContext context) {
        Object arguments = context.getArguments();
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
