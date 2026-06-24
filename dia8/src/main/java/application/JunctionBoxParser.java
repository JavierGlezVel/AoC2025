package application;

import domain.common.JunctionBox;

import java.util.List;

public class JunctionBoxParser {
    public List<JunctionBox> parse(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("A playground needs at least one junction box");
        }

        return lines.stream()
                .map(this::parseLine)
                .toList();
    }

    private JunctionBox parseLine(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Junction box line cannot be null");
        }

        String[] parts = line.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid junction box position: " + line);
        }

        return new JunctionBox(
                parseCoordinate(parts[0], line),
                parseCoordinate(parts[1], line),
                parseCoordinate(parts[2], line)
        );
    }

    private long parseCoordinate(String text, String line) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid junction box coordinate in line: " + line, exception);
        }
    }
}
