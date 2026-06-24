package domain.part2;

import application.PaperRollMapParser;
import domain.common.PaperRollMap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RemovablePaperRollCounterPart2Test {
    @Test
    void countsRemovablePaperRollsFromOfficialExample() {
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

        int removablePaperRolls = new RemovablePaperRollCounterPart2().count(map);

        assertEquals(43, removablePaperRolls);
    }

    @Test
    void keepsRemovingPaperRollsThatBecomeAccessibleLater() {
        PaperRollMap map = new PaperRollMap(List.of(
                "@@@",
                "@@@",
                "@@@"
        ));

        int removablePaperRolls = new RemovablePaperRollCounterPart2().count(map);

        assertEquals(9, removablePaperRolls);
    }
}
