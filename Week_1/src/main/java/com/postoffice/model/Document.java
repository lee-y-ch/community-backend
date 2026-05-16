package com.postoffice.model;

public class Document extends Standard {

    // Document는 무게 상관없이 고정된 가격 책정이 이루어진다.
    public Document(String trackingNumber, boolean isGlobal) {
        // Document는 MaxWeight를 따질 필요가 없기 때문에 무게 제한을 MAX_VALUE로 넣어준다.
        super(trackingNumber, 0.0, isGlobal, Double.MAX_VALUE);
    }

    @Override
    protected long calculateFee() {
        return 3000;
    }
}
