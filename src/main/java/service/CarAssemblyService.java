package service;

import model.*;

public class CarAssemblyService {

    private final CompatibilityChecker checker;

    public CarAssemblyService(CompatibilityChecker checker) {
        this.checker = checker;
    }

    public RunResult run(CarSpec spec) {
        ViolationResult v = checker.check(spec);
        if (!v.isValid())
            return RunResult.failure("자동차가 동작되지 않습니다\n" + v.getReason());

        if (spec.getEngine().isBroken())
            return RunResult.failure("엔진이 고장나있습니다.\n자동차가 움직이지 않습니다.");

        String summary = String.format(
            "Car Type : %s\nEngine   : %s\nBrake    : %s\nSteering : %s\n자동차가 동작됩니다.",
            spec.getCarType().getDisplayName(),
            spec.getEngine().getDisplayName(),
            spec.getBrakeSystem().getDisplayName(),
            spec.getSteeringSystem().getDisplayName()
        );
        return RunResult.success(summary);
    }

    public TestResult test(CarSpec spec) {
        ViolationResult v = checker.check(spec);
        if (!v.isValid())
            return TestResult.fail(v.getReason());
        return TestResult.pass();
    }
}
