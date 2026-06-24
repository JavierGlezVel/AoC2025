package domain.common;

public record ClosedInterval(long start, long end) {
    public ClosedInterval {
        if (start > end) {
            throw new IllegalArgumentException("Interval start must be <= end");
        }
    }

    public boolean contains(ClosedInterval other) {
        return start <= other.start && other.end <= end;
    }

    public boolean touchesOrOverlaps(ClosedInterval other) {
        return start <= other.end + 1 && other.start <= end + 1;
    }

    public ClosedInterval merge(ClosedInterval other) {
        if (!touchesOrOverlaps(other)) {
            throw new IllegalArgumentException("Intervals do not touch or overlap");
        }
        return new ClosedInterval(Math.min(start, other.start), Math.max(end, other.end));
    }
}
