package com.gemfire.model;

public class FxRate {
    private String fromCurrency;
    private String toCurrency;
    private int fxRate;
    private String date;

    public FxRate(String fromCurrency, String toCurrency, int fxRate, String date) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fxRate = fxRate;
        this.date = date;
    }

    public FxRate() {
        this("", "", 1, "");
    }
}