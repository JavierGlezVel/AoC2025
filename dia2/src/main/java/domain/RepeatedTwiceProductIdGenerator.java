package domain;

import java.util.ArrayList;
import java.util.List;

public class RepeatedTwiceProductIdGenerator {
    public List<Long> generateUntil(long maxId) {
        List<Long> invalidIds = new ArrayList<>();

        for (int digits = 1; ; digits++) {
            long firstPrefix = powerOfTen(digits - 1);
            long lastPrefix = powerOfTen(digits) - 1;
            long firstIdWithDigits = repeatTwice(firstPrefix);

            if (firstIdWithDigits > maxId) {
                return invalidIds;
            }

            for (long prefix = firstPrefix; prefix <= lastPrefix; prefix++) {
                long invalidId = repeatTwice(prefix);
                if (invalidId > maxId) {
                    break;
                }
                invalidIds.add(invalidId);
            }
        }
    }

    private long repeatTwice(long prefix) {
        String digits = String.valueOf(prefix);
        return Long.parseLong(digits + digits);
    }

    private long powerOfTen(int exponent) {
        long value = 1;
        for (int i = 0; i < exponent; i++) {
            value *= 10;
        }
        return value;
    }
}
