package com.postoffice.model;

public abstract class Standard extends Package {

    protected double maxWeight;

    public Standard(String trackingNumber, double weight, boolean isGlobal, double maxWeight) {
        super(trackingNumber, weight, isGlobal);
        this.maxWeight = maxWeight;
    }

    public boolean isOverWeight() {
        return weight > maxWeight;
    }

    @Override
    public void printLabel() {
        super.printLabel();
        if (isOverWeight()) {
            System.out.println("경고: 중량 초과 (" + weight + "kg / 최대 " +
                    maxWeight + "kg)");
            System.out.println("--------------------");
        }
    }
}
