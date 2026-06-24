package domain.common;

import java.math.BigInteger;
import java.util.List;

public enum MathOperation {
    ADD('+') {
        @Override
        public BigInteger apply(List<BigInteger> numbers) {
            return numbers.stream()
                    .reduce(BigInteger.ZERO, BigInteger::add);
        }
    },
    MULTIPLY('*') {
        @Override
        public BigInteger apply(List<BigInteger> numbers) {
            return numbers.stream()
                    .reduce(BigInteger.ONE, BigInteger::multiply);
        }
    };

    private final char symbol;

    MathOperation(char symbol) {
        this.symbol = symbol;
    }

    public static MathOperation fromSymbol(char symbol) {
        for (MathOperation operation : values()) {
            if (operation.symbol == symbol) {
                return operation;
            }
        }
        throw new IllegalArgumentException("Operation must be + or *");
    }

    public char symbol() {
        return symbol;
    }

    public abstract BigInteger apply(List<BigInteger> numbers);
}
