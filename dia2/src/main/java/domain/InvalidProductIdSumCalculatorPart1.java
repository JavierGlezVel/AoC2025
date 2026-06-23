package domain;

import java.util.List;

public class InvalidProductIdSumCalculatorPart1 {
    public long calculate(List<ProductIdRange> ranges) {
        long maxId = ranges.stream()
                .mapToLong(ProductIdRange::lastId)
                .max()
                .orElse(0);

        List<Long> invalidIds = new RepeatedTwiceProductIdGenerator().generateUntil(maxId);
        return new InvalidProductIdSumCalculator().calculate(ranges, invalidIds);
    }
}
