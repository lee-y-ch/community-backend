package com.postoffice.model;

public abstract class Package {

    protected String trackingNumber; //송장번호
    protected double weight; //택배 무게
    protected boolean isGlobal; //해외 배송 유무

    // 생성자를 통해 각 속성을 주입받기
    public Package(String trackingNumber, double weight, boolean isGlobal) {
        this.trackingNumber = trackingNumber;
        this.weight = weight;
        this.isGlobal = isGlobal;
    }

    // 자식 클래스에서만 구현하면 되는 메서드이기 때문에 protected로 선언
    protected abstract long calculateFee();

    // Main에서 최종 가격 출력을 위해 public으로 선언하고, 자식 클래스에서 오버라이딩 하지 못하게 final로 선언한다.
    public final long getFinalFee() {
        // 각 하위 클래스에서 계산한 금액에서,
        // 해외 배송이면 가격이 2배이고 국내이면 그대로 책정
        if (isGlobal) {
            return calculateFee() * 2;
        } else {
            return calculateFee();
        }
    }

    // Main에서 송장번호를 출력해주기 위해 public으로 선언
    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void printLabel() {
        String region;
        if (isGlobal) {
            region = "해외 배송";
        } else {
            region = "국내 배송";
        }

        System.out.println("--------------------");
        System.out.println("운송장 번호: " + trackingNumber);
        System.out.println("배송 구분: " + region);
        System.out.println("최종 요금: " + getFinalFee() + "원");
        System.out.println("--------------------");
    }
}
