package domain.part1;

import application.JunctionBoxParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CircuitSizeProductCalculatorPart1Test {
    @Test
    void multipliesThreeLargestCircuitsFromOfficialExample() {
        var junctionBoxes = new JunctionBoxParser().parse(List.of(
                "162,817,812",
                "57,618,57",
                "906,360,560",
                "592,479,940",
                "352,342,300",
                "466,668,158",
                "542,29,236",
                "431,825,988",
                "739,650,466",
                "52,470,668",
                "216,146,977",
                "819,987,18",
                "117,168,530",
                "805,96,715",
                "346,949,466",
                "970,615,88",
                "941,993,340",
                "862,61,35",
                "984,92,344",
                "425,690,689"
        ));

        long product = new CircuitSizeProductCalculatorPart1().calculate(junctionBoxes, 10);

        assertEquals(40, product);
    }

    @Test
    void alreadyConnectedPairsStillCountAsProcessedConnections() {
        var junctionBoxes = new JunctionBoxParser().parse(List.of(
                "0,0,0",
                "1,0,0",
                "2,0,0",
                "100,0,0"
        ));

        long product = new CircuitSizeProductCalculatorPart1().calculate(junctionBoxes, 3);

        assertEquals(3, product);
    }
}
