package domain.part1;

import domain.common.RedTile;

import java.util.List;

public class LargestRectangleAreaCalculatorPart1 {
    public long calculate(List<RedTile> redTiles) {
        if (redTiles == null || redTiles.size() < 2) {
            throw new IllegalArgumentException("At least two red tiles are needed");
        }

        long largestArea = 0;
        for (int first = 0; first < redTiles.size(); first++) {
            for (int second = first + 1; second < redTiles.size(); second++) {
                long area = redTiles.get(first).rectangleAreaWith(redTiles.get(second));
                largestArea = Math.max(largestArea, area);
            }
        }

        return largestArea;
    }
}
