package application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedTileParserTest {
    @Test
    void parsesRedTileCoordinates() {
        var redTiles = new RedTileParser().parse(List.of(
                "7,1",
                "11,7"
        ));

        assertEquals(2, redTiles.size());
        assertEquals(7, redTiles.getFirst().x());
        assertEquals(1, redTiles.getFirst().y());
    }

    @Test
    void rejectsInvalidLines() {
        RedTileParser parser = new RedTileParser();

        assertThrows(IllegalArgumentException.class, () -> parser.parse(List.of("1")));
        assertThrows(IllegalArgumentException.class, () -> parser.parse(List.of("1,two")));
    }
}
