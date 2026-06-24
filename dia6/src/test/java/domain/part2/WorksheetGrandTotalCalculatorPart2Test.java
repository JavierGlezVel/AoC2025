package domain.part2;

import application.MathWorksheetParser;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorksheetGrandTotalCalculatorPart2Test {
    @Test
    void sumsProblemResultsFromOfficialExample() {
        var problems = new MathWorksheetParser().parseRightToLeft(List.of(
                "123 328  51 64 ",
                " 45 64  387 23 ",
                "  6 98  215 314",
                "*   +   *   +  "
        ));

        BigInteger grandTotal = new WorksheetGrandTotalCalculatorPart2().calculate(problems);

        assertEquals(BigInteger.valueOf(3_263_827), grandTotal);
    }
}
