package application;

import domain.ProductIdRange;

import java.util.ArrayList;
import java.util.List;

public class ProductIdRangeParser {
    public List<ProductIdRange> parse(String input) {
        List<ProductIdRange> ranges = new ArrayList<>();
        String normalizedInput = input.strip();
        if (normalizedInput.isEmpty()) {
            return ranges;
        }

        for (String rawRange : normalizedInput.split(",")) {
            String[] limits = rawRange.trim().split("-");
            if (limits.length != 2) {
                throw new IllegalArgumentException("Invalid range: " + rawRange);
            }
            long firstId = Long.parseLong(limits[0]);
            long lastId = Long.parseLong(limits[1]);
            ranges.add(new ProductIdRange(firstId, lastId));
        }
        return ranges;
    }
}
