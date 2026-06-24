package domain.part1;

import application.PaperRollMapParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessiblePaperRollCounterPart1Test {
    @Test
    void countsAccessiblePaperRollsFromOfficialExample() {
        var map = new PaperRollMapParser().parse(List.of(
                "..@@.@@@@.",
                "@@@.@.@.@@",
                "@@@@@.@.@@",
                "@.@@@@..@.",
                "@@.@@@@.@@",
                ".@@@@@@@.@",
                ".@.@.@.@@@",
                "@.@@@.@@@@",
                ".@@@@@@@@.",
                "@.@.@@@.@."
        ));

        int accessiblePaperRolls = new AccessiblePaperRollCounterPart1().count(map);

        assertEquals(13, accessiblePaperRolls);
    }
}
