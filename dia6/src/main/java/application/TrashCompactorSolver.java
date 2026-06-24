package application;

import domain.part1.WorksheetGrandTotalCalculatorPart1;
import domain.part2.WorksheetGrandTotalCalculatorPart2;
import infrastructure.WorksheetSource;

import java.io.IOException;
import java.math.BigInteger;

public class TrashCompactorSolver {
    private final WorksheetSource source;
    private final MathWorksheetParser parser;

    public TrashCompactorSolver(WorksheetSource source) {
        this.source = source;
        this.parser = new MathWorksheetParser();
    }

    public BigInteger solvePart1() throws IOException {
        var problems = parser.parse(source.getLines());
        return new WorksheetGrandTotalCalculatorPart1().calculate(problems);
    }

    public BigInteger solvePart2() throws IOException {
        var problems = parser.parseRightToLeft(source.getLines());
        return new WorksheetGrandTotalCalculatorPart2().calculate(problems);
    }
}
