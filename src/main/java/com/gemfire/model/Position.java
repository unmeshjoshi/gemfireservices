package com.gemfire.model;

public class Position {
    private Integer accountKey;
    private String accountType;
    private String accountNumber;
    private String assetClassL1;
    private String assetClassL2;
    private String securityId;
    private Integer quantity;
    private String accountGroupId;
    private Integer balance;
    private String currency;
    private String positionDate;

    public Position(Integer accountKey, String accountType, String accountNumber, String assetClassL1, String assetClassL2, String securityId, Integer quantity, String accountGroupId, Integer balance, String currency, String positionDate) {
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
}
