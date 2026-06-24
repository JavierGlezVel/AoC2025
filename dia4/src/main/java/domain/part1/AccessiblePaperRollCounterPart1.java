package domain.part1;

import domain.common.GridPosition;
import domain.common.PaperRollMap;

public class AccessiblePaperRollCounterPart1 {
    private static final int MAXIMUM_ADJACENT_ROLLS_TO_ACCESS = 3;

    public int count(PaperRollMap map) {
        int accessiblePaperRolls = 0;

        for (int row = 0; row < map.height(); row++) {
            for (int column = 0; column < map.width(); column++) {
                GridPosition position = new GridPosition(row, column);
                if (isAccessiblePaperRoll(map, position)) {
                    accessiblePaperRolls++;
                }
            }
        }

        return accessiblePaperRolls;
    }

    private boolean isAccessiblePaperRoll(PaperRollMap map, GridPosition position) {
        return map.isPaperRollAt(position)
                && map.countAdjacentPaperRolls(position) <= MAXIMUM_ADJACENT_ROLLS_TO_ACCESS;
    }
}
