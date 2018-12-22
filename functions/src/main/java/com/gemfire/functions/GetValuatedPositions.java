package com.gemfire.functions;

import com.gemfire.models.FxRate;
import com.gemfire.models.MarketPrice;
import com.gemfire.models.Position;
import com.gemfire.models.ValuatedPosition;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.partition.PartitionRegionHelper;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.internal.logging.LogService;
import org.apache.geode.security.ResourcePermission;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetValuatedPositions implements Function, Declarable {

    private void time(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        long end = System.nanoTime();
        long timeTaken = TimeUnit.NANOSECONDS.toMillis(end - start);
        LogService.getLogger().info("Time taken for function execution is " + timeTaken + " ");
    }

    @Override
    public Collection<ResourcePermission> getRequiredPermissions(String regionName) {
        StringWriter writer = new StringWriter();
        Exception e  = new Exception();
        e.printStackTrace(new PrintWriter(writer));

        LogService.getLogger().info("Getting permissions for " + writer.toString());
        return Collections.singletonList(new ResourcePermission(ResourcePermission.Resource.DATA, ResourcePermission.Operation.READ, "financialservices"));
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public void execute(FunctionContext context) {
        time(() -> {
            RegionFunctionContext rctx = (RegionFunctionContext) context;
            Region<Object, Position> positionRegion = rctx.getDataSet();

            Args args = (Args) context.getArguments();
            int[] acctKeys = new int[]{1, 1};
            String reportingCurrency = "USD";


            List<ValuatedPosition> valuatedPositions = calculatePositions(rctx, positionRegion, acctKeys, reportingCurrency);
            rctx.getResultSender().lastResult(valuatedPositions);
        });
    }

    private List<ValuatedPosition> calculatePositions(RegionFunctionContext rctx, Region<Object, Position> positionRegion, int[] acctKeys, String reportingCurrency) {
        Cache cache = CacheFactory.getAnyInstance();

        Region<Object, FxRate> fxRates = cache.getRegion("/FxRates");
        Region<Object, MarketPrice> marketPrices = cache.getRegion("/MarketPrices");

        //pass member id to function
        String member = "memberid";
        Region<Object, List<String>> visibilityRegion = cache.getRegion("/Visibility");
        Collection<List<String>> visibleAccountKeys = visibilityRegion.getAll(Arrays.asList(member, "1", "2", "3", "4")).values();

        try {
            List<ValuatedPosition> positionResult = new ArrayList<>();

            for (int i = 0; i < acctKeys.length; i++) {
                Region<Object, Object> localDataForContext = PartitionRegionHelper.getLocalDataForContext(rctx);
                SelectResults<Position> result = localDataForContext.query("accountKey = " + acctKeys[i]);
                List<Position> serializedPositions = result.asList(); //TODO: Remove OQL access
                if (null == serializedPositions) {
                    continue;
                }
                valuatePosition(reportingCurrency, fxRates, marketPrices, positionResult, serializedPositions);
            }
            return positionResult;

        } catch (Exception e) {
            throw new RuntimeException(e);
//            ArrayList lastResult = new ArrayList();
//            lastResult.add(e.getMessage());
//            rctx.getResultSender().lastResult(lastResult);
        }
    }

    private void valuatePosition(String reportingCurrency, Region<Object, FxRate> fxRates, Region<Object, MarketPrice> marketPrices, List<ValuatedPosition> positionResult, List<Position> positions) {
        for (Position position : positions) {
            String positionCurrency = position.getCurrency();
            FxRate exchangeRate = (FxRate) fxRates.get(FxRate.keyFrom(positionCurrency, reportingCurrency));
            String securityId = position.getSecurityId();
            MarketPrice marketPrice = marketPrices.get(securityId);
            positionResult.add(new ValuatedPosition(position, exchangeRate, marketPrice));
        }
    }

    @Override
    public String getId() {
        return "GetValuedPositions";
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
