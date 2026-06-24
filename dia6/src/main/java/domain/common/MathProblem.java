package domain.common;

import java.math.BigInteger;
import java.util.List;

public record MathProblem(List<BigInteger> numbers, MathOperation operation) {
    public MathProblem {
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("A math problem needs at least one number");
        }
        numbers = List.copyOf(numbers);
        if (operation == null) {
            throw new IllegalArgumentException("Operation cannot be null");
        }
    }
}
