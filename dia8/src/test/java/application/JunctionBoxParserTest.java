package application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JunctionBoxParserTest {
    @Test
    void parsesJunctionBoxCoordinates() {
        var junctionBoxes = new JunctionBoxParser().parse(List.of(
                "162,817,812",
                "57,618,57"
        ));

        assertEquals(2, junctionBoxes.size());
        assertEquals(162, junctionBoxes.getFirst().x());
        assertEquals(817, junctionBoxes.getFirst().y());
        assertEquals(812, junctionBoxes.getFirst().z());
    }

    @Test
    void rejectsInvalidLines() {
        JunctionBoxParser parser = new JunctionBoxParser();

        assertThrows(IllegalArgumentException.class, () -> parser.parse(List.of("1,2")));
        assertThrows(IllegalArgumentException.class, () -> parser.parse(List.of("1,two,3")));
    }
}
