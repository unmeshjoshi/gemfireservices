package com.gemfire.models;

public class DerivedPosition {
    private Position position;
    private FxRate fxRate;

    public DerivedPosition(Position position, FxRate fxRate) {
        this.position = position;
        this.fxRate = fxRate;
    }
}
