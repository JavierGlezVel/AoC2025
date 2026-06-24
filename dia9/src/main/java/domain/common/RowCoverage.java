package domain.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record RowCoverage(long firstY, long lastY, List<ClosedInterval> xIntervals) {
    public RowCoverage {
        if (firstY > lastY) {
            throw new IllegalArgumentException("First row must be <= last row");
        }
        xIntervals = mergeIntervals(xIntervals);
    }

    public boolean intersectsRows(long first, long last) {
        return firstY <= last && first <= lastY;
    }

    public boolean containsXInterval(ClosedInterval interval) {
        return xIntervals.stream().anyMatch(xInterval -> xInterval.contains(interval));
    }

    private static List<ClosedInterval> mergeIntervals(List<ClosedInterval> intervals) {
        if (intervals == null || intervals.isEmpty()) {
            return List.of();
        }

        List<ClosedInterval> sortedIntervals = intervals.stream()
                .sorted(Comparator.comparingLong(ClosedInterval::start))
                .toList();
        List<ClosedInterval> mergedIntervals = new ArrayList<>();

        for (ClosedInterval interval : sortedIntervals) {
            if (mergedIntervals.isEmpty()) {
                mergedIntervals.add(interval);
                continue;
            }

            ClosedInterval last = mergedIntervals.getLast();
            if (last.touchesOrOverlaps(interval)) {
                mergedIntervals.set(mergedIntervals.size() - 1, last.merge(interval));
            } else {
                mergedIntervals.add(interval);
            }
        }

        return List.copyOf(mergedIntervals);
    }
}
