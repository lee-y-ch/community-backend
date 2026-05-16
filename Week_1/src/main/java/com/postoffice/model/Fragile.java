package com.postoffice.model;

public class Fragile extends Special {

    private String handlingInstruction;

    public Fragile(String trackingNumber, double weight, boolean isGlobal, long additionalServiceFee, String handlingInstruction) {
        super(trackingNumber, weight, isGlobal, additionalServiceFee);
        this.handlingInstruction = handlingInstruction;
    }


    @Override
    public void printLabel() {
        // 부모 클래스의 printLabel() 재사용을 위해 super 활용
        super.printLabel();
        System.out.println("파손 주의: " + handlingInstruction);
        System.out.println("--------------------");
    }
}
