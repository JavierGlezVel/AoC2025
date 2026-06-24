package domain.part2;

import application.RedTileParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LargestContainedRectangleAreaCalculatorPart2Test {
    @Test
    void findsLargestContainedRectangleFromOfficialExample() {
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

        long largestArea = new LargestContainedRectangleAreaCalculatorPart2().calculate(redTiles);

        assertEquals(24, largestArea);
    }

    @Test
    void rejectsRectanglesThatCrossAConcavity() {
        var redTiles = new RedTileParser().parse(List.of(
                "0,0",
                "4,0",
                "4,4",
                "3,4",
                "3,1",
                "1,1",
                "1,4",
                "0,4"
        ));

        long largestArea = new LargestContainedRectangleAreaCalculatorPart2().calculate(redTiles);

        assertEquals(10, largestArea);
    }
}
