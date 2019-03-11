package com.gemfire.functions;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.internal.logging.LogService;
import org.apache.geode.pdx.PdxInstance;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

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
        LogService.getLogger().info(BaseArgs.class + " loaded from " + getClassLoaderJar(BaseArgs.class));
        LogService.getLogger().info(args.getClass() + " args class parent is " + args.getClass().getClassLoader().getParent());
        LogService.getLogger().info(args.getClass() + " loaded from " + getClassLoaderJar(args.getClass()));
        LogService.getLogger().info(this.getClass() + " loaded from " + getClassLoaderJar(this.getClass()));

        System.out.println("args = " + args);
        Integer first = (Integer) args.getI1();
        Integer second = (Integer) args.getI2();
        try {
            LogService.getLogger().info("Function sleeping for 4 minutes");
            Thread.sleep(1000); //sleep for 4 minutes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogService.getLogger().info("Function returning result " + this.getClass() + " loaded from " + this.getClass().getClassLoader());

        rctx.getResultSender().lastResult(first * second);
    }

    private List<URL> getClassLoaderJar(Class clazz) {
        URL[] urls = ((URLClassLoader) clazz.getClassLoader()).getURLs();
        return Arrays.asList(urls);
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
