package com.gemfire.models;

public class FxRate {
    private String fromCurrency;
    private String toCurrency;
    private int fxRate;
    private String forDate;

    public FxRate() {
    }

    public FxRate(String fromCurrency, String toCurrency, int fxRate, String forDate) {
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

    public int getFxRate() {
        return fxRate;
    }

    public String getForDate() {
        return forDate;
    }
}