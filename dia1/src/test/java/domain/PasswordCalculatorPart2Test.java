package domain;

import application.RotationParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordCalculatorPart2Test {
    @Test
    void countsTimesDialPassesThroughZeroDuringRotations() {
        List<Rotation> rotations = new RotationParser().parse(List.of(
                "L68",
                "L30",
                "R48",
                "L5",
                "R60",
                "L55",
                "L1",
                "L99",
                "R14",
                "L82"
        ));

        int password = new PasswordCalculatorPart2().calculate(rotations);

        assertEquals(6, password);
    }

    @Test
    void countsMultipleFullTurnsWithoutCountingTheInitialPosition() {
        List<Rotation> rotations = new RotationParser().parse(List.of("R250"));

        int password = new PasswordCalculatorPart2().calculate(rotations);

        assertEquals(3, password);
    }

    @Test
    void countsTenZerosForOfficialR1000Warning() {
        List<Rotation> rotations = new RotationParser().parse(List.of("R1000"));

        int password = new PasswordCalculatorPart2().calculate(rotations);

        assertEquals(10, password);
    }
}
