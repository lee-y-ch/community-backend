package com.postoffice.model;

public class NormalBox extends Standard {

    public NormalBox(String trackingNumber, double weight, boolean isGlobal) {
        super(trackingNumber, weight, isGlobal, 30.0);
    }

    @Override
    protected long calculateFee() {
        // 숫자 계산이 이루어질 때 부동 소수점 문제를 방지하기 위해 double이 아닌 long 사용
        return (long)(weight * 500);
    }

}
