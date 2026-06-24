package application;

import domain.common.MathProblem;
import domain.common.MathOperation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MathWorksheetParser {
    public List<MathProblem> parse(List<String> lines) {
        List<String> normalizedLines = normalize(lines);
        List<ColumnRange> problemRanges = findProblemRanges(normalizedLines);
        List<MathProblem> problems = new ArrayList<>();

        for (ColumnRange range : problemRanges) {
            problems.add(parseProblemTopToBottom(normalizedLines, range));
        }

        return problems;
    }

    public List<MathProblem> parseRightToLeft(List<String> lines) {
        List<String> normalizedLines = normalize(lines);
        List<ColumnRange> problemRanges = findProblemRanges(normalizedLines);
        List<MathProblem> problems = new ArrayList<>();

        for (int i = problemRanges.size() - 1; i >= 0; i--) {
            problems.add(parseProblemRightToLeft(normalizedLines, problemRanges.get(i)));
        }

        return problems;
    }

    private List<String> normalize(List<String> lines) {
        if (lines == null || lines.size() < 2) {
            throw new IllegalArgumentException("A worksheet needs number rows and an operation row");
        }

        int width = lines.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        return normalizeLines(lines, width);
    }

    private List<ColumnRange> findProblemRanges(List<String> lines) {
        int width = lines.getFirst().length();
        List<ColumnRange> problemRanges = new ArrayList<>();
        int column = 0;

        while (column < width) {
            while (column < width && isBlankColumn(lines, column)) {
                column++;
            }
            if (column == width) {
                break;
            }

            int startColumn = column;
            while (column < width && !isBlankColumn(lines, column)) {
                column++;
            }
            problemRanges.add(new ColumnRange(startColumn, column));
        }

        return problemRanges;
    }

    private List<String> normalizeLines(List<String> lines, int width) {
        return lines.stream()
                .map(line -> line + " ".repeat(width - line.length()))
                .toList();
    }

    private boolean isBlankColumn(List<String> lines, int column) {
        return lines.stream().allMatch(line -> line.charAt(column) == ' ');
    }

    private MathProblem parseProblemTopToBottom(List<String> lines, ColumnRange range) {
        List<BigInteger> numbers = new ArrayList<>();
        int operationRowIndex = lines.size() - 1;

        for (int row = 0; row < operationRowIndex; row++) {
            String value = lines.get(row).substring(range.startColumn(), range.endColumn()).trim();
            if (!value.isEmpty()) {
                numbers.add(new BigInteger(value));
            }
        }

        return new MathProblem(numbers, parseOperation(lines, range));
    }

    private MathProblem parseProblemRightToLeft(List<String> lines, ColumnRange range) {
        List<BigInteger> numbers = new ArrayList<>();
        int operationRowIndex = lines.size() - 1;

        for (int column = range.endColumn() - 1; column >= range.startColumn(); column--) {
            StringBuilder value = new StringBuilder();
            for (int row = 0; row < operationRowIndex; row++) {
                char digit = lines.get(row).charAt(column);
                if (digit != ' ') {
                    value.append(digit);
                }
            }
            if (!value.isEmpty()) {
                numbers.add(new BigInteger(value.toString()));
            }
        }

        return new MathProblem(numbers, parseOperation(lines, range));
    }

    private MathOperation parseOperation(List<String> lines, ColumnRange range) {
        int operationRowIndex = lines.size() - 1;
        String operationText = lines.get(operationRowIndex)
                .substring(range.startColumn(), range.endColumn())
                .trim();
        if (operationText.length() != 1) {
            throw new IllegalArgumentException("Invalid operation: " + operationText);
        }

        return MathOperation.fromSymbol(operationText.charAt(0));
    }

    private record ColumnRange(int startColumn, int endColumn) {
    }
}
