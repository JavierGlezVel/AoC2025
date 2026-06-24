package application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperRollMapParserTest {
    @Test
    void parsesNonBlankRows() {
        var map = new PaperRollMapParser().parse(List.of(
                ".@.",
                "",
                " @@. "
        ));

        assertEquals(2, map.height());
        assertEquals(3, map.width());
    }

    @Test
    void rejectsNonRectangularMaps() {
        PaperRollMapParser parser = new PaperRollMapParser();

        assertThrows(IllegalArgumentException.class, () -> parser.parse(List.of("@@", "@")));
    }
}
