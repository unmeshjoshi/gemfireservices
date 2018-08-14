package com.gemfire.models;

import java.math.BigDecimal;

public class FxRate {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal fxRate;
    private String forDate;

    public FxRate() {
    }

    public FxRate(String fromCurrency, String toCurrency, BigDecimal fxRate, String forDate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fxRate = fxRate;
        this.forDate = forDate;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public BigDecimal getFxRate() {
        return fxRate;
    }

    public String getForDate() {
        return forDate;
    }


    public String key() {
        return getFromCurrency() + "_" + getToCurrency();
    }
}