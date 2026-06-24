package domain.part2;

import domain.common.GridPosition;
import domain.common.PaperRollMap;

import java.util.ArrayDeque;
import java.util.Queue;

public class RemovablePaperRollCounterPart2 {
    private static final int MAXIMUM_ADJACENT_ROLLS_TO_ACCESS = 3;

    public int count(PaperRollMap map) {
        boolean[][] remainingPaperRolls = copyPaperRolls(map);
        int[][] adjacentPaperRolls = countInitialAdjacentPaperRolls(map);
        boolean[][] queuedPaperRolls = new boolean[map.height()][map.width()];
        Queue<GridPosition> accessiblePaperRolls = new ArrayDeque<>();

        enqueueAccessiblePaperRolls(map, remainingPaperRolls, adjacentPaperRolls, queuedPaperRolls, accessiblePaperRolls);

        int removedPaperRolls = 0;
        while (!accessiblePaperRolls.isEmpty()) {
            GridPosition position = accessiblePaperRolls.remove();
            if (!remainingPaperRolls[position.row()][position.column()]) {
                continue;
            }

            remainingPaperRolls[position.row()][position.column()] = false;
            removedPaperRolls++;
            updateAdjacentPaperRolls(map, position, remainingPaperRolls, adjacentPaperRolls,
                    queuedPaperRolls, accessiblePaperRolls);
        }

        return removedPaperRolls;
    }

    private boolean[][] copyPaperRolls(PaperRollMap map) {
        boolean[][] paperRolls = new boolean[map.height()][map.width()];

        for (int row = 0; row < map.height(); row++) {
            for (int column = 0; column < map.width(); column++) {
                GridPosition position = new GridPosition(row, column);
                paperRolls[row][column] = map.isPaperRollAt(position);
            }
        }

        return paperRolls;
    }

    private int[][] countInitialAdjacentPaperRolls(PaperRollMap map) {
        int[][] adjacentPaperRolls = new int[map.height()][map.width()];

        for (int row = 0; row < map.height(); row++) {
            for (int column = 0; column < map.width(); column++) {
                GridPosition position = new GridPosition(row, column);
                if (map.isPaperRollAt(position)) {
                    adjacentPaperRolls[row][column] = map.countAdjacentPaperRolls(position);
                }
            }
        }

        return adjacentPaperRolls;
    }

    private void enqueueAccessiblePaperRolls(
            PaperRollMap map,
            boolean[][] remainingPaperRolls,
            int[][] adjacentPaperRolls,
            boolean[][] queuedPaperRolls,
            Queue<GridPosition> accessiblePaperRolls
    ) {
        for (int row = 0; row < map.height(); row++) {
            for (int column = 0; column < map.width(); column++) {
                enqueueIfAccessible(new GridPosition(row, column), remainingPaperRolls,
                        adjacentPaperRolls, queuedPaperRolls, accessiblePaperRolls);
            }
        }
    }

    private void updateAdjacentPaperRolls(
            PaperRollMap map,
            GridPosition removedPosition,
            boolean[][] remainingPaperRolls,
            int[][] adjacentPaperRolls,
            boolean[][] queuedPaperRolls,
            Queue<GridPosition> accessiblePaperRolls
    ) {
        for (GridPosition adjacent : map.adjacentPositions(removedPosition)) {
            if (!remainingPaperRolls[adjacent.row()][adjacent.column()]) {
                continue;
            }

            adjacentPaperRolls[adjacent.row()][adjacent.column()]--;
            enqueueIfAccessible(adjacent, remainingPaperRolls, adjacentPaperRolls,
                    queuedPaperRolls, accessiblePaperRolls);
        }
    }

    private void enqueueIfAccessible(
            GridPosition position,
            boolean[][] remainingPaperRolls,
            int[][] adjacentPaperRolls,
            boolean[][] queuedPaperRolls,
            Queue<GridPosition> accessiblePaperRolls
    ) {
        if (remainingPaperRolls[position.row()][position.column()]
                && adjacentPaperRolls[position.row()][position.column()] <= MAXIMUM_ADJACENT_ROLLS_TO_ACCESS
                && !queuedPaperRolls[position.row()][position.column()]) {
            accessiblePaperRolls.add(position);
            queuedPaperRolls[position.row()][position.column()] = true;
        }
    }
}
