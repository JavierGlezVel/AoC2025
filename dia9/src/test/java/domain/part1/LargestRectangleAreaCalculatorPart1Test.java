package domain.part1;

import application.RedTileParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LargestRectangleAreaCalculatorPart1Test {
    @Test
    void findsLargestRectangleFromOfficialExample() {
        var redTiles = new RedTileParser().parse(List.of(
                "7,1",
                "11,1",
                "11,7",
                "9,7",
                "9,5",
                "2,5",
                "2,3",
                "7,3"
        ));

        long largestArea = new LargestRectangleAreaCalculatorPart1().calculate(redTiles);

        assertEquals(50, largestArea);
    }

    @Test
    void usesInclusiveRectangleDimensions() {
        var redTiles = new RedTileParser().parse(List.of(
                "2,5",
                "9,7"
        ));

        long largestArea = new LargestRectangleAreaCalculatorPart1().calculate(redTiles);

        assertEquals(24, largestArea);
    }
}
