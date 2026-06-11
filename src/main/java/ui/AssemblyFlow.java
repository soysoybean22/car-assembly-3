package ui;

import model.*;
import service.CarAssemblyService;

public class AssemblyFlow {

    private static final int CAR_TYPE_Q = 0;
    private static final int ENGINE_Q   = 1;
    private static final int BRAKE_Q    = 2;
    private static final int STEERING_Q = 3;
    private static final int RUN_TEST   = 4;

    private final InputHandler       input;
    private final MenuPrinter        printer;
    private final CarAssemblyService service;

    public AssemblyFlow(InputHandler input, MenuPrinter printer, CarAssemblyService service) {
        this.input   = input;
        this.printer = printer;
        this.service = service;
    }

    public void run() {
        CarSpec spec = new CarSpec();
        int step = CAR_TYPE_Q;

        while (true) {
            printer.clearScreen();
            printMenu(step);

            int answer = input.readInt();
            if (answer == -1) { printer.printMessage("바이바이"); break; }

            if (!isValidRange(step, answer)) continue;

            if (answer == 0) {
                if (step == RUN_TEST) { spec.reset(); step = CAR_TYPE_Q; }
                else if (step > CAR_TYPE_Q) step--;
                continue;
            }

            step = applyAnswer(step, answer, spec);
        }
    }

    private void printMenu(int step) {
        switch (step) {
            case CAR_TYPE_Q: printer.printCarTypeMenu();  break;
            case ENGINE_Q:   printer.printEngineMenu();   break;
            case BRAKE_Q:    printer.printBrakeMenu();    break;
            case STEERING_Q: printer.printSteeringMenu(); break;
            case RUN_TEST:   printer.printRunTestMenu();  break;
        }
    }

    private boolean isValidRange(int step, int ans) {
        int max;
        switch (step) {
            case CAR_TYPE_Q: max = 3; break;
            case ENGINE_Q:   max = 4; break;
            case BRAKE_Q:    max = 3; break;
            case STEERING_Q: max = 2; break;
            case RUN_TEST:   max = 2; break;
            default:         return false;
        }
        int min = (step == CAR_TYPE_Q) ? 1 : 0;
        if (ans < min || ans > max) {
            printer.printMessage("ERROR :: 유효한 번호를 입력하세요 (" + min + "~" + max + ")");
            return false;
        }
        return true;
    }

    private int applyAnswer(int step, int answer, CarSpec spec) {
        switch (step) {
            case CAR_TYPE_Q:
                spec.setCarType(CarType.fromMenuNumber(answer));
                printer.printMessage("차량 타입으로 " + spec.getCarType().getDisplayName() + "을 선택하셨습니다.");
                delay(800);
                return ENGINE_Q;
            case ENGINE_Q:
                spec.setEngine(Engine.fromMenuNumber(answer));
                printer.printMessage(spec.getEngine().getDisplayName() + " 엔진을 선택하셨습니다.");
                delay(800);
                return BRAKE_Q;
            case BRAKE_Q:
                spec.setBrakeSystem(BrakeSystem.fromMenuNumber(answer));
                printer.printMessage(spec.getBrakeSystem().getDisplayName() + " 제동장치를 선택하셨습니다.");
                delay(800);
                return STEERING_Q;
            case STEERING_Q:
                spec.setSteeringSystem(SteeringSystem.fromMenuNumber(answer));
                printer.printMessage(spec.getSteeringSystem().getDisplayName() + " 조향장치를 선택하셨습니다.");
                delay(800);
                return RUN_TEST;
            case RUN_TEST:
                if (answer == 1) {
                    printer.printRunResult(service.run(spec));
                } else {
                    printer.printMessage("Test...");
                    printer.printTestResult(service.test(spec));
                }
                delay(2000);
                return RUN_TEST;
            default:
                return step;
        }
    }

    private void delay(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
