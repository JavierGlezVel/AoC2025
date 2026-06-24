package domain.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperRollMapTest {
    @Test
    void countsOnlyExistingAdjacentPositionsAtTheMapBorder() {
        PaperRollMap map = new PaperRollMap(List.of(
                "@@",
                "@."
        ));

        int adjacentPaperRolls = map.countAdjacentPaperRolls(new GridPosition(0, 0));

        assertEquals(2, adjacentPaperRolls);
    }

    @Test
    void returnsOnlyExistingAdjacentPositionsAtTheMapBorder() {
        PaperRollMap map = new PaperRollMap(List.of(
                "@@",
                "@."
        ));

        var adjacentPositions = map.adjacentPositions(new GridPosition(0, 0));

        assertEquals(3, adjacentPositions.size());
    }

    @Test
    void rejectsUnexpectedCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new PaperRollMap(List.of("@x")));
    }
}
