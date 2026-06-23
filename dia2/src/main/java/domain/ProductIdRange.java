package domain;

public record ProductIdRange(long firstId, long lastId) {
    public ProductIdRange {
        if (firstId < 0 || lastId < 0) {
            throw new IllegalArgumentException("Range limits must be >= 0");
        }
        if (firstId > lastId) {
            throw new IllegalArgumentException("First ID must be <= last ID");
        }
    }

    public boolean contains(long id) {
        return firstId <= id && id <= lastId;
    }
}
