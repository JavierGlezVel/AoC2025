package application;

import domain.part1.CircuitSizeProductCalculatorPart1;
import domain.part2.FinalConnectionXProductCalculatorPart2;
import infrastructure.JunctionBoxSource;

import java.io.IOException;

public class PlaygroundSolver {
    private static final int CONNECTIONS_TO_PROCESS_PART_1 = 1000;

    private final JunctionBoxSource source;
    private final JunctionBoxParser parser;

    public PlaygroundSolver(JunctionBoxSource source) {
        this.source = source;
        this.parser = new JunctionBoxParser();
    }

    public long solvePart1() throws IOException {
        var junctionBoxes = parser.parse(source.getLines());
        return new CircuitSizeProductCalculatorPart1().calculate(junctionBoxes, CONNECTIONS_TO_PROCESS_PART_1);
    }

    public long solvePart2() throws IOException {
        var junctionBoxes = parser.parse(source.getLines());
        return new FinalConnectionXProductCalculatorPart2().calculate(junctionBoxes);
    }
}
