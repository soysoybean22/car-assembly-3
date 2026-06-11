import service.CarAssemblyService;
import service.CompatibilityChecker;
import ui.AssemblyFlow;
import ui.InputHandler;
import ui.MenuPrinter;
import java.util.Scanner;

public class Assemble {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        CompatibilityChecker checker = new CompatibilityChecker();
        CarAssemblyService   service = new CarAssemblyService(checker);
        InputHandler         input   = new InputHandler(sc);
        MenuPrinter          printer = new MenuPrinter();
        new AssemblyFlow(input, printer, service).run();
        sc.close();
    }
}
