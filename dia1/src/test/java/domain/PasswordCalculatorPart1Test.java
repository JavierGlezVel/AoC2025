package domain;

import application.RotationParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordCalculatorPart1Test {
    @Test
    void countsTimesDialEndsAtZeroAfterARotation() {
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

        int password = new PasswordCalculatorPart1().calculate(rotations);

        assertEquals(3, password);
    }
}
