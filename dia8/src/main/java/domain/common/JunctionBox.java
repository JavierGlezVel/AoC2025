package domain.common;

public record JunctionBox(long x, long y, long z) {
    public long distanceSquaredTo(JunctionBox other) {
        long dx = x - other.x;
        long dy = y - other.y;
        long dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
