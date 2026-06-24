package domain.common;

import java.util.ArrayList;
import java.util.List;

public record PaperRollMap(List<String> rows) {
    public PaperRollMap {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("A paper roll map needs at least one row");
        }
        rows = List.copyOf(rows);
        int width = rows.getFirst().length();
        if (width == 0) {
            throw new IllegalArgumentException("A paper roll map needs at least one column");
        }

        for (String row : rows) {
            validateRow(row, width);
        }
    }

    public int height() {
        return rows.size();
    }

    public int width() {
        return rows.getFirst().length();
    }

    public boolean isPaperRollAt(GridPosition position) {
        return rows.get(position.row()).charAt(position.column()) == '@';
    }

    public boolean contains(GridPosition position) {
        return 0 <= position.row()
                && position.row() < height()
                && 0 <= position.column()
                && position.column() < width();
    }

    public int countAdjacentPaperRolls(GridPosition position) {
        int adjacentPaperRolls = 0;

        for (GridPosition adjacent : adjacentPositions(position)) {
            if (isPaperRollAt(adjacent)) {
                adjacentPaperRolls++;
            }
        }

        return adjacentPaperRolls;
    }

    public List<GridPosition> adjacentPositions(GridPosition position) {
        List<GridPosition> adjacentPositions = new ArrayList<>();

        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                if (rowOffset == 0 && columnOffset == 0) {
                    continue;
                }

                GridPosition adjacent = new GridPosition(
                        position.row() + rowOffset,
                        position.column() + columnOffset
                );
                if (contains(adjacent)) {
                    adjacentPositions.add(adjacent);
                }
            }
        }

        return adjacentPositions;
    }

    private static void validateRow(String row, int width) {
        if (row == null || row.length() != width) {
            throw new IllegalArgumentException("All rows in a paper roll map must have the same width");
        }
        if (!row.matches("[.@]+")) {
            throw new IllegalArgumentException("Map rows can only contain '.' and '@': " + row);
        }
    }
}
