package com.gemfire.models;

import java.math.BigDecimal;

public class Position {
    private Integer accountKey;
    private String accountType;
    private String accountNumber;

    private String ppCode;
    private String subPpCode;
    private String region;
    private boolean isRealTime;
    private String countryOfPurchase;

    private String assetClassL1;
    private String assetClassL2;
    private String securityId;
    private Integer quantity;
    private String accountGroupId;
    private BigDecimal balance;
    private String currency;
    private String positionDate;

    public Position(Integer accountKey, String accountType, String accountNumber, String assetClassL1, String assetClassL2, String securityId, Integer quantity, String accountGroupId, BigDecimal balance, String currency, String positionDate) {
        this.accountKey = accountKey;
        this.accountType = accountType;
        this.accountNumber = accountNumber;
        this.assetClassL1 = assetClassL1;
        this.assetClassL2 = assetClassL2;
        this.securityId = securityId;
        this.quantity = quantity;
        this.accountGroupId = accountGroupId;
        this.balance = balance;
        this.currency = currency;
        this.positionDate = positionDate;
    }

    public Position() {
    }

    public String key() {
        return accountKey + "_" + positionDate + "_" + hashCode();
    }

    public Integer getAccountKey() {
        return accountKey;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAssetClassL1() {
        return assetClassL1;
    }

    public String getAssetClassL2() {
        return assetClassL2;
    }

    public String getSecurityId() {
        return securityId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getAccountGroupId() {
        return accountGroupId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPositionDate() {
        return positionDate;
    }

    public int calculateBalance() {
        return balance.multiply(BigDecimal.valueOf(2)).intValue();
    }
}
