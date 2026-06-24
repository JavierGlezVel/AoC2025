package domain.part1;

import domain.common.CircuitNetwork;
import domain.common.ConnectionCandidate;
import domain.common.ConnectionCandidateGenerator;
import domain.common.JunctionBox;

import java.util.List;

public class CircuitSizeProductCalculatorPart1 {
    private static final int CIRCUITS_TO_MULTIPLY = 3;

    public long calculate(List<JunctionBox> junctionBoxes, int connectionsToProcess) {
        if (junctionBoxes == null || junctionBoxes.size() < CIRCUITS_TO_MULTIPLY) {
            throw new IllegalArgumentException("At least three junction boxes are needed");
        }
        if (connectionsToProcess < 0) {
            throw new IllegalArgumentException("Connections to process must be >= 0");
        }

        List<ConnectionCandidate> candidates = new ConnectionCandidateGenerator().generateSorted(junctionBoxes);
        CircuitNetwork network = new CircuitNetwork(junctionBoxes.size());
        int processedConnections = Math.min(connectionsToProcess, candidates.size());

        for (int i = 0; i < processedConnections; i++) {
            ConnectionCandidate candidate = candidates.get(i);
            network.connect(candidate.firstIndex(), candidate.secondIndex());
        }

        return network.largestCircuitSizes(CIRCUITS_TO_MULTIPLY).stream()
                .mapToLong(Integer::longValue)
                .reduce(1L, (left, right) -> left * right);
    }
}
