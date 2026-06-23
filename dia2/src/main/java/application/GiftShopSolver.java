package application;

import domain.InvalidProductIdSumCalculatorPart1;
import domain.InvalidProductIdSumCalculatorPart2;
import infrastructure.RangeSource;

import java.io.IOException;

public class GiftShopSolver {
    private final RangeSource source;
    private final ProductIdRangeParser parser;

    public GiftShopSolver(RangeSource source) {
        this.source = source;
        this.parser = new ProductIdRangeParser();
    }

    public long solvePart1() throws IOException {
        var ranges = parser.parse(source.getContent());
        return new InvalidProductIdSumCalculatorPart1().calculate(ranges);
    }

    public long solvePart2() throws IOException {
        var ranges = parser.parse(source.getContent());
        return new InvalidProductIdSumCalculatorPart2().calculate(ranges);
    }
}
