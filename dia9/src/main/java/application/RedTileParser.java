package application;

import domain.common.RedTile;

import java.util.List;

public class RedTileParser {
    public List<RedTile> parse(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("A theater floor needs at least one red tile");
        }

        return lines.stream()
                .map(this::parseLine)
                .toList();
    }

    private RedTile parseLine(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Red tile line cannot be null");
        }

        String[] parts = line.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid red tile position: " + line);
        }

        return new RedTile(parseCoordinate(parts[0], line), parseCoordinate(parts[1], line));
    }

    private long parseCoordinate(String text, String line) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid red tile coordinate in line: " + line, exception);
        }
    }
}
