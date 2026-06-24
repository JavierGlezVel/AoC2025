package domain.common;

public record RedTile(long x, long y) {
    public long rectangleAreaWith(RedTile other) {
        long width = Math.abs(x - other.x) + 1;
        long height = Math.abs(y - other.y) + 1;
        return width * height;
    }
}
