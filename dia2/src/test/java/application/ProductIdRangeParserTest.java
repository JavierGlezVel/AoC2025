package application;

import domain.ProductIdRange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductIdRangeParserTest {
    @Test
    void parsesCommaSeparatedRanges() {
        List<ProductIdRange> ranges = new ProductIdRangeParser().parse("11-22,95-115");

        assertEquals(List.of(
                new ProductIdRange(11, 22),
                new ProductIdRange(95, 115)
        ), ranges);
    }
}
