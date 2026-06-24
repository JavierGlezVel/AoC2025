package domain.common;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class RedGreenTileArea {
    private final List<RedTile> vertices;
    private final List<RowCoverage> rowCoverages;

    public RedGreenTileArea(List<RedTile> vertices) {
        if (vertices == null || vertices.size() < 4) {
            throw new IllegalArgumentException("A red-green tile area needs at least four vertices");
        }
        this.vertices = List.copyOf(vertices);
        validateOrthogonalLoop();
        this.rowCoverages = buildRowCoverages();
    }

    public boolean containsRectangle(RedTile firstCorner, RedTile secondCorner) {
        long firstX = Math.min(firstCorner.x(), secondCorner.x());
        long lastX = Math.max(firstCorner.x(), secondCorner.x());
        long firstY = Math.min(firstCorner.y(), secondCorner.y());
        long lastY = Math.max(firstCorner.y(), secondCorner.y());
        ClosedInterval xInterval = new ClosedInterval(firstX, lastX);

        return rowCoverages.stream()
                .filter(rowCoverage -> rowCoverage.intersectsRows(firstY, lastY))
                .allMatch(rowCoverage -> rowCoverage.containsXInterval(xInterval));
    }

    private void validateOrthogonalLoop() {
        for (int i = 0; i < vertices.size(); i++) {
            RedTile current = vertices.get(i);
            RedTile next = vertices.get((i + 1) % vertices.size());
            if (current.x() != next.x() && current.y() != next.y()) {
                throw new IllegalArgumentException("Adjacent red tiles must share a row or a column");
            }
        }
    }

    private List<RowCoverage> buildRowCoverages() {
        List<Long> yCoordinates = sortedYCoordinates();
        List<RowCoverage> coverages = new ArrayList<>();

        for (long y : yCoordinates) {
            coverages.add(new RowCoverage(y, y, exactRowIntervals(y)));
        }

        for (int i = 0; i < yCoordinates.size() - 1; i++) {
            long lowerY = yCoordinates.get(i);
            long upperY = yCoordinates.get(i + 1);
            long firstInteriorRow = lowerY + 1;
            long lastInteriorRow = upperY - 1;

            if (firstInteriorRow <= lastInteriorRow) {
                coverages.add(new RowCoverage(firstInteriorRow, lastInteriorRow, openRowIntervals(lowerY + 0.5)));
            }
        }

        return List.copyOf(coverages);
    }

    private List<Long> sortedYCoordinates() {
        TreeSet<Long> yCoordinates = new TreeSet<>();
        for (RedTile vertex : vertices) {
            yCoordinates.add(vertex.y());
        }
        return List.copyOf(yCoordinates);
    }

    private List<ClosedInterval> exactRowIntervals(long y) {
        List<ClosedInterval> intervals = new ArrayList<>();
        intervals.addAll(openRowIntervals(y - 0.5));
        intervals.addAll(openRowIntervals(y + 0.5));
        intervals.addAll(horizontalBoundaryIntervals(y));
        return intervals;
    }

    private List<ClosedInterval> openRowIntervals(double y) {
        List<Long> intersections = new ArrayList<>();

        for (int i = 0; i < vertices.size(); i++) {
            RedTile current = vertices.get(i);
            RedTile next = vertices.get((i + 1) % vertices.size());
            if (current.x() == next.x()) {
                long lowerY = Math.min(current.y(), next.y());
                long upperY = Math.max(current.y(), next.y());
                if (lowerY < y && y < upperY) {
                    intersections.add(current.x());
                }
            }
        }

        intersections.sort(Long::compare);
        List<ClosedInterval> intervals = new ArrayList<>();
        for (int i = 0; i + 1 < intersections.size(); i += 2) {
            intervals.add(new ClosedInterval(intersections.get(i), intersections.get(i + 1)));
        }
        return intervals;
    }

    private List<ClosedInterval> horizontalBoundaryIntervals(long y) {
        List<ClosedInterval> intervals = new ArrayList<>();

        for (int i = 0; i < vertices.size(); i++) {
            RedTile current = vertices.get(i);
            RedTile next = vertices.get((i + 1) % vertices.size());
            if (current.y() == next.y() && current.y() == y) {
                intervals.add(new ClosedInterval(
                        Math.min(current.x(), next.x()),
                        Math.max(current.x(), next.x())
                ));
            }
        }

        return intervals;
    }
}
