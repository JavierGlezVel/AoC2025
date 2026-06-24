import application.PrintingDepartmentSolver;
import infrastructure.DiagramSource;
import infrastructure.FileDiagramSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        String inputPath = args.length > 0 ? args[0] : defaultInputPath();

        DiagramSource source = new FileDiagramSource(inputPath);
        PrintingDepartmentSolver solver = new PrintingDepartmentSolver(source);
        System.out.println("Parte 1: " + solver.solvePart1());
        System.out.println("Parte 2: " + solver.solvePart2());
    }

    private static String defaultInputPath() {
        Path modulePath = Path.of("src", "main", "resources", "input.txt");
        if (Files.exists(modulePath)) {
            return modulePath.toString();
        }
        return Path.of("dia4", "src", "main", "resources", "input.txt").toString();
    }
}
