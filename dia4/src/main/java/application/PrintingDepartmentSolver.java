package application;

import domain.part1.AccessiblePaperRollCounterPart1;
import domain.part2.RemovablePaperRollCounterPart2;
import infrastructure.DiagramSource;

import java.io.IOException;

public class PrintingDepartmentSolver {
    private final DiagramSource source;
    private final PaperRollMapParser parser;

    public PrintingDepartmentSolver(DiagramSource source) {
        this.source = source;
        this.parser = new PaperRollMapParser();
    }

    public int solvePart1() throws IOException {
        var map = parser.parse(source.getLines());
        return new AccessiblePaperRollCounterPart1().count(map);
    }

    public int solvePart2() throws IOException {
        var map = parser.parse(source.getLines());
        return new RemovablePaperRollCounterPart2().count(map);
    }
}
