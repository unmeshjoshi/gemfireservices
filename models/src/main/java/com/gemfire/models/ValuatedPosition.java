package com.gemfire.models;

public class ValuatedPosition {
    private Position position;
    private FxRate fxRate;
    private MarketPrice marketPrice;

    public ValuatedPosition(){}

    public ValuatedPosition(Position position, FxRate fxRate, MarketPrice marketPrice) {
        this.position = position;
        this.fxRate = fxRate;
        this.marketPrice = marketPrice;
    }

    public Position getPosition() {
        return position;
    }

    public FxRate getFxRate() {
        return fxRate;
    }

    public MarketPrice getMarketPrice() {
        return marketPrice;
    }
}
