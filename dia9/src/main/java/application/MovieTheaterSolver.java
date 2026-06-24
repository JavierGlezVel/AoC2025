package application;

import domain.part1.LargestRectangleAreaCalculatorPart1;
import domain.part2.LargestContainedRectangleAreaCalculatorPart2;
import infrastructure.RedTileSource;

import java.io.IOException;

public class MovieTheaterSolver {
    private final RedTileSource source;
    private final RedTileParser parser;

    public MovieTheaterSolver(RedTileSource source) {
        this.source = source;
        this.parser = new RedTileParser();
    }

    public long solvePart1() throws IOException {
        var redTiles = parser.parse(source.getLines());
        return new LargestRectangleAreaCalculatorPart1().calculate(redTiles);
    }

    public long solvePart2() throws IOException {
        var redTiles = parser.parse(source.getLines());
        return new LargestContainedRectangleAreaCalculatorPart2().calculate(redTiles);
    }
}
