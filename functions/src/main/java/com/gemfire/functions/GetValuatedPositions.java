package com.gemfire.functions;

import com.gemfire.models.Position;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.cache.query.Struct;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GetValuatedPositions implements Function {
    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public void execute(FunctionContext context) {
        RegionFunctionContext rctx = (RegionFunctionContext)context;
        Region<Object, Position> dataSet = rctx.getDataSet();
        Object[] args = (Object[]) context.getArguments();
        Integer[] acctKeys = (Integer[]) args[0];
        String reportingCurrency = (String) args[1];

        try {
//            List<Position> positions = dataSet.<Position>query("accountKey in SET(1, 2)").asList();
//            List<String> positionCurrencies = positions.stream().map(p -> p.getCurrency()).collect(Collectors.toList());
//            System.out.println(positionCurrencies + "******************************");
            Cache cache = CacheFactory.getAnyInstance();
            QueryService queryService = cache.getQueryService();
            Query query = queryService.newQuery("select fx from /FxRates");
            List<Struct> structs = ((SelectResults<Struct>) query.execute()).asList();
            List<Object> fx = structs.stream().map(p -> p.get("fx")).collect(Collectors.toList());
            System.out.println(fx + "+++++++++++++++++++++++++++++++");

            rctx.getResultSender().lastResult(fx);
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
