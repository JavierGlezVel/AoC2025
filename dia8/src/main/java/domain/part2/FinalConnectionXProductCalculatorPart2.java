package domain.part2;

import domain.common.CircuitNetwork;
import domain.common.ConnectionCandidate;
import domain.common.ConnectionCandidateGenerator;
import domain.common.JunctionBox;

import java.util.List;

public class FinalConnectionXProductCalculatorPart2 {
    public long calculate(List<JunctionBox> junctionBoxes) {
        if (junctionBoxes == null || junctionBoxes.size() < 2) {
            throw new IllegalArgumentException("At least two junction boxes are needed");
        }

        List<ConnectionCandidate> candidates = new ConnectionCandidateGenerator().generateSorted(junctionBoxes);
        CircuitNetwork network = new CircuitNetwork(junctionBoxes.size());

        for (ConnectionCandidate candidate : candidates) {
            boolean connectedDifferentCircuits = network.connect(candidate.firstIndex(), candidate.secondIndex());
            if (connectedDifferentCircuits && network.isSingleCircuit()) {
                JunctionBox first = junctionBoxes.get(candidate.firstIndex());
                JunctionBox second = junctionBoxes.get(candidate.secondIndex());
                return first.x() * second.x();
            }
        }

        throw new IllegalStateException("Junction boxes could not be connected into a single circuit");
    }
}
