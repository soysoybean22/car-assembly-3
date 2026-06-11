package service;

import model.*;

public class CompatibilityChecker {

    public ViolationResult check(CarSpec spec) {
        if (spec.getCarType() == CarType.SEDAN
                && spec.getBrakeSystem() == BrakeSystem.CONTINENTAL)
            return ViolationResult.fail("Sedan에는 Continental 제동장치 사용 불가");

        if (spec.getCarType() == CarType.SUV
                && spec.getEngine() == Engine.TOYOTA)
            return ViolationResult.fail("SUV에는 TOYOTA 엔진 사용 불가");

        if (spec.getCarType() == CarType.TRUCK
                && spec.getEngine() == Engine.WIA)
            return ViolationResult.fail("Truck에는 WIA 엔진 사용 불가");

        if (spec.getCarType() == CarType.TRUCK
                && spec.getBrakeSystem() == BrakeSystem.MANDO)
            return ViolationResult.fail("Truck에는 Mando 제동장치 사용 불가");

        if (spec.getBrakeSystem() == BrakeSystem.BOSCH
                && spec.getSteeringSystem() != SteeringSystem.BOSCH)
            return ViolationResult.fail("Bosch 제동장치에는 Bosch 조향장치 이외 사용 불가");

        return ViolationResult.pass();
    }
}
