package application;

import domain.common.PaperRollMap;

import java.util.ArrayList;
import java.util.List;

public class PaperRollMapParser {
    public PaperRollMap parse(List<String> lines) {
        List<String> rows = new ArrayList<>();

        for (String line : lines) {
            String row = line.trim();
            if (!row.isEmpty()) {
                rows.add(row);
            }
        }

        return new PaperRollMap(rows);
    }
}
