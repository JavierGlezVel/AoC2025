package domain;

import application.ProductIdRangeParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidProductIdSumCalculatorPart1Test {
    @Test
    void sumsInvalidIdsFromOfficialExample() {
        String input = "11-22,95-115,998-1012,1188511880-1188511890,222220-222224,"
                + "1698522-1698528,446443-446449,38593856-38593862,565653-565659,"
                + "824824821-824824827,2121212118-2121212124";
        List<ProductIdRange> ranges = new ProductIdRangeParser().parse(input);

        long sum = new InvalidProductIdSumCalculatorPart1().calculate(ranges);

        assertEquals(1227775554L, sum);
    }

    @Test
    void doesNotAddTheSameInvalidIdTwiceWhenRangesOverlap() {
        List<ProductIdRange> ranges = new ProductIdRangeParser().parse("10-20,15-25");

        long sum = new InvalidProductIdSumCalculatorPart1().calculate(ranges);

        assertEquals(33, sum);
    }
}
