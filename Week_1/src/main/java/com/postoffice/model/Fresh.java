package com.postoffice.model;

public class Fresh extends Special {

    private double temperature;

    public Fresh(String trackingNumber, double weight, boolean isGlobal, long additionalServiceFee, double temperature) {
        super(trackingNumber, weight, isGlobal, additionalServiceFee);
        this.temperature = temperature;
    }

    @Override
    public void printLabel() {
        // 부모 클래스의 printLabel() 재사용을 위해 super 활용
        super.printLabel();
        System.out.println(temperature + "도씨의 온도를 유지해주세요");
        System.out.println("--------------------");
    }
}
