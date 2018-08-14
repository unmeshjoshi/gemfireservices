package com.gemfire.models;

import java.math.BigDecimal;

public class MarketPrice {
        String symbol;
        BigDecimal openingPrice;
        BigDecimal closingPrice;
        BigDecimal high;
        BigDecimal low;

    public MarketPrice(String symbol, BigDecimal openingPrice, BigDecimal closingPrice, BigDecimal high, BigDecimal low) {
        this.openingPrice = openingPrice;
        this.closingPrice = closingPrice;
        this.high = high;
        this.low = low;
    }

    public BigDecimal getOpeningPrice() {
        return openingPrice;
    }

    public BigDecimal getClosingPrice() {
        return closingPrice;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public String getSymbol() {
        return symbol;
    }

    public String key() {
        return symbol;
    }
}
