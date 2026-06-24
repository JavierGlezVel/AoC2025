package domain.part2;

import domain.common.RedGreenTileArea;
import domain.common.RedTile;

import java.util.List;

public class LargestContainedRectangleAreaCalculatorPart2 {
    public long calculate(List<RedTile> redTiles) {
        if (redTiles == null || redTiles.size() < 4) {
            throw new IllegalArgumentException("At least four red tiles are needed");
        }

        RedGreenTileArea area = new RedGreenTileArea(redTiles);
        long largestArea = 0;

        for (int first = 0; first < redTiles.size(); first++) {
            for (int second = first + 1; second < redTiles.size(); second++) {
                RedTile firstCorner = redTiles.get(first);
                RedTile secondCorner = redTiles.get(second);
                long rectangleArea = firstCorner.rectangleAreaWith(secondCorner);

                if (rectangleArea > largestArea && area.containsRectangle(firstCorner, secondCorner)) {
                    largestArea = rectangleArea;
                }
            }
        }

        return largestArea;
    }
}
