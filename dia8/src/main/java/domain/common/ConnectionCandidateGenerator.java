package domain.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConnectionCandidateGenerator {
    public List<ConnectionCandidate> generateSorted(List<JunctionBox> junctionBoxes) {
        List<ConnectionCandidate> candidates = new ArrayList<>();

        for (int first = 0; first < junctionBoxes.size(); first++) {
            for (int second = first + 1; second < junctionBoxes.size(); second++) {
                long distanceSquared = junctionBoxes.get(first).distanceSquaredTo(junctionBoxes.get(second));
                candidates.add(new ConnectionCandidate(first, second, distanceSquared));
            }
        }

        candidates.sort(Comparator.comparingLong(ConnectionCandidate::distanceSquared)
                .thenComparingInt(ConnectionCandidate::firstIndex)
                .thenComparingInt(ConnectionCandidate::secondIndex));
        return candidates;
    }
}
