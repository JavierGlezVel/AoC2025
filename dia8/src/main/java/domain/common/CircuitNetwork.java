package domain.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CircuitNetwork {
    private final int[] parents;
    private final int[] sizes;
    private int circuitCount;

    public CircuitNetwork(int elements) {
        if (elements < 1) {
            throw new IllegalArgumentException("A circuit network needs at least one element");
        }

        this.parents = new int[elements];
        this.sizes = new int[elements];
        this.circuitCount = elements;
        for (int i = 0; i < elements; i++) {
            parents[i] = i;
            sizes[i] = 1;
        }
    }

    public boolean connect(int first, int second) {
        int firstRoot = find(first);
        int secondRoot = find(second);

        if (firstRoot == secondRoot) {
            return false;
        }

        if (sizes[firstRoot] < sizes[secondRoot]) {
            int temporary = firstRoot;
            firstRoot = secondRoot;
            secondRoot = temporary;
        }

        parents[secondRoot] = firstRoot;
        sizes[firstRoot] += sizes[secondRoot];
        circuitCount--;
        return true;
    }

    public boolean isSingleCircuit() {
        return circuitCount == 1;
    }

    public List<Integer> largestCircuitSizes(int count) {
        List<Integer> circuitSizes = new ArrayList<>();
        for (int i = 0; i < parents.length; i++) {
            if (find(i) == i) {
                circuitSizes.add(sizes[i]);
            }
        }

        return circuitSizes.stream()
                .sorted(Comparator.reverseOrder())
                .limit(count)
                .toList();
    }

    private int find(int element) {
        if (parents[element] != element) {
            parents[element] = find(parents[element]);
        }
        return parents[element];
    }
}
