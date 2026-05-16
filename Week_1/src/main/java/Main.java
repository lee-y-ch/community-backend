import com.postoffice.model.*;
import com.postoffice.model.Package;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== 우체국 택배 접수 ===");

        System.out.println("배송 지역을 선택하세요. (1: 국내, 2: 국제)");
        boolean isGlobal = false;
        if (sc.nextInt() == 2) {
            isGlobal = true;
        }

        System.out.println("택배 종류를 선택하세요");
        System.out.println("1: 서류, 2: 일반 박스, 3: 파손 주의, 4: 신선식품");
        int type = sc.nextInt();

        // UUID를 활용하여 매번 바뀌는 값으로 해야하지만, 이 값이 중요한 사항이 아니기 떄문에 "123"으로 통일
        String trackingNumber = "123";

        Package myPackage = null;
        switch (type) {
            case 1:
                myPackage = new Document(trackingNumber, isGlobal);
                break;
            case 2:
                System.out.println("무게(kg)를 입력하세요: ");
                myPackage = new NormalBox(trackingNumber, sc.nextDouble(), isGlobal);
                break;
            case 3:
                System.out.println("무게(kg)를 입력하세요: ");
                double fragileWeight = sc.nextDouble();

                sc.nextLine();

                System.out.println("취급 시 주의사항을 입력하세요: ");
                String instruction = sc.nextLine();

                myPackage = new Fragile(trackingNumber, fragileWeight, isGlobal, 2000, instruction);
                break;
            case 4:
                System.out.println("무게(kg) 및 유지해야할 온도(도씨)를 입력하세요: ");
                myPackage = new Fresh(trackingNumber, sc.nextDouble(), isGlobal, 2000, sc.nextDouble());
                break;
        }

        if (myPackage != null) {
            myPackage.printLabel();
        } else {
            System.out.println("다시 시도하세요.");
        }
    }
}
