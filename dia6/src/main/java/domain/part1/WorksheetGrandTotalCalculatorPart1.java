package domain.part1;

import domain.common.MathProblem;

import java.math.BigInteger;
import java.util.List;

public class WorksheetGrandTotalCalculatorPart1 {
    public BigInteger calculate(List<MathProblem> problems) {
        return problems.stream()
                .map(problem -> problem.operation().apply(problem.numbers()))
                .reduce(BigInteger.ZERO, BigInteger::add);
    }
}
