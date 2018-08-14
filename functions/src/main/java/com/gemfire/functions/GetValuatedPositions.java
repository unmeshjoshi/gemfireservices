package com.gemfire.functions;

import com.gemfire.models.FxRate;
import com.gemfire.models.MarketPrice;
import com.gemfire.models.Position;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;

import java.util.ArrayList;
import java.util.List;

public class GetValuatedPositions implements Function {
    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public void execute(FunctionContext context) {
        RegionFunctionContext rctx = (RegionFunctionContext)context;
        Region<Object, Position> positionRegion = rctx.getDataSet();

        Cache cache = CacheFactory.getAnyInstance();
        Region<Object, FxRate> fxRates = cache.getRegion("/FxRates");
        Region<Object, MarketPrice> marketPrices = cache.getRegion("/MarketPrices");

        Object[] args = (Object[]) context.getArguments();
        Integer[] acctKeys = (Integer[]) args[0];
        String reportingCurrency = (String) args[1];

        try {

            List<Position> positions = new ArrayList<>();
            for(int i = 0; i < acctKeys.length; i++) {
                Position position = positionRegion.get(acctKeys[i]);
                String positionCurrency = position.getCurrency();
                FxRate exchangeRate = (FxRate) fxRates.get(positionCurrency + "_" + reportingCurrency);

            }


            rctx.getResultSender().lastResult(positions);
        } catch (Exception e) {
            e.printStackTrace();
            ArrayList lastResult = new ArrayList();
            lastResult.add(e.getMessage());
            rctx.getResultSender().lastResult(lastResult);
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
