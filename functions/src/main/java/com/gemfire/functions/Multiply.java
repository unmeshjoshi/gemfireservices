package com.gemfire.functions;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.internal.logging.LogService;
import org.apache.geode.pdx.PdxInstance;

public class Multiply implements Function {
    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public void execute(FunctionContext context) {
        RegionFunctionContext rctx = (RegionFunctionContext)context;
        Region<Object, Object> dataSet = rctx.getDataSet();
        MultArgs args = (MultArgs) ((PdxInstance)context.getArguments()).getObject();

        LogService.getLogger().info("Thread context classloader is " + Thread.currentThread().getContextClassLoader());
        LogService.getLogger().info(args.getClass() + " args class parent is " + args.getClass().getClassLoader().getParent());
        LogService.getLogger().info(args.getClass() + " loaded from " + args.getClass().getClassLoader());
        LogService.getLogger().info(this.getClass() + " loaded from " + this.getClass().getClassLoader());

        System.out.println("args = " + args);
        Integer first = (Integer) args.getI1();
        Integer second = (Integer) args.getI2();
//        try {
            LogService.getLogger().info("Function sleeping for 4 minutes");
//            Thread.sleep(240020); //sleep for 4 minutes
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        LogService.getLogger().info("Function returning result " + this.getClass() + " loaded from " + this.getClass().getClassLoader());

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
