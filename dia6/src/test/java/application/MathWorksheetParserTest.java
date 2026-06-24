package application;

import domain.common.MathOperation;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MathWorksheetParserTest {
    @Test
    void parsesProblemsSeparatedByBlankColumns() {
        var problems = new MathWorksheetParser().parse(List.of(
                "123 328  51 64 ",
                " 45 64  387 23 ",
                "  6 98  215 314",
                "*   +   *   +  "
        ));

        assertEquals(4, problems.size());
        assertEquals(List.of(BigInteger.valueOf(123), BigInteger.valueOf(45), BigInteger.valueOf(6)),
                problems.getFirst().numbers());
        assertEquals(MathOperation.MULTIPLY, problems.getFirst().operation());
        assertEquals(MathOperation.ADD, problems.getLast().operation());
    }

    @Test
    void acceptsRowsWithDifferentWidths() {
        var problems = new MathWorksheetParser().parse(List.of(
                "12  7",
                " 3  8",
                "*   +  "
        ));

        assertEquals(2, problems.size());
        assertEquals(List.of(BigInteger.valueOf(12), BigInteger.valueOf(3)), problems.getFirst().numbers());
        assertEquals(List.of(BigInteger.valueOf(7), BigInteger.valueOf(8)), problems.getLast().numbers());
    }

    @Test
    void rejectsUnknownOperations() {
        MathWorksheetParser parser = new MathWorksheetParser();

        assertThrows(IllegalArgumentException.class, () -> parser.parse(List.of(
                "12",
                "-"
        )));
    }

    @Test
    void parsesRightToLeftProblemsSeparatedByBlankColumns() {
        var problems = new MathWorksheetParser().parseRightToLeft(List.of(
                "123 328  51 64 ",
                " 45 64  387 23 ",
                "  6 98  215 314",
                "*   +   *   +  "
        ));

        assertEquals(4, problems.size());
        assertEquals(List.of(BigInteger.valueOf(4), BigInteger.valueOf(431), BigInteger.valueOf(623)),
                problems.getFirst().numbers());
        assertEquals(MathOperation.ADD, problems.getFirst().operation());
        assertEquals(List.of(BigInteger.valueOf(356), BigInteger.valueOf(24), BigInteger.ONE),
                problems.getLast().numbers());
        assertEquals(MathOperation.MULTIPLY, problems.getLast().operation());
    }
}
