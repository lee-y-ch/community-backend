package com.postoffice.model;

public abstract class Special extends Package {

    private long additionalServiceFee;

    public Special(String trackingNumber, double weight, boolean isGlobal, long additionalServiceFee) {
        super(trackingNumber, weight, isGlobal);
        this.additionalServiceFee = additionalServiceFee;
    }

    @Override
    protected long calculateFee() {
        return (long)(weight * 500) + additionalServiceFee;
    }

}
