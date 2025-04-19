package org.example;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

public class CrossoverTesting {

    // Utility method to create a list of Locations with ID starting from 1
    private List<Location> generateLocations(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    double lat = -90 + (i % 180);   // range [-90, 89]
                    double lon = -180 + (i % 360);  // range [-180, 179]
                    return new Location(lat, lon, i + 1);
                })
                .collect(Collectors.toList());
    }


    @Test
    void testCrossoverProducesValidChild() {
        List<Location> locations = generateLocations(6);

        Individual parent1 = new Individual(new ArrayList<>(locations), true);
        Individual parent2 = new Individual(Arrays.asList(
                locations.get(0), locations.get(3), locations.get(5),
                locations.get(1), locations.get(4), locations.get(2)
        ), true);

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 0.0, 2, 2, 0, 42, locations, null, true
        );

        Individual child = ga.crossover(parent1, parent2);

        assertEquals(locations.get(0), child.route.get(0), "Index 0 should match parent1");
        assertEquals(6, child.route.size(), "Child should have same length as parents");
        assertEquals(new HashSet<>(locations), new HashSet<>(child.route), "Child should have all unique locations");
    }

    @Test
    void testCrossoverWithIdenticalParentsReturnsClone() {
        List<Location> locations = generateLocations(5);
        Individual parent = new Individual(new ArrayList<>(locations), true);

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 0.0, 2, 2, 0, 123, locations, null, true
        );

        Individual child = ga.crossover(parent, parent);

        assertEquals(parent.route, child.route, "Crossover with identical parents should return parent");
    }

    @Test
    void testCrossoverWithTwoLocations() {
        List<Location> locations = Arrays.asList(
                new Location(0, 0, 1),
                new Location(1, 1, 2)
        );

        Individual parent1 = new Individual(locations, true);
        Individual parent2 = new Individual(Arrays.asList(
                locations.get(0), locations.get(1)
        ), true);

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 0.0, 2, 2, 0, 456, locations, null, true
        );

        Individual child = ga.crossover(parent1, parent2);

        assertEquals(locations.size(), child.route.size(), "Child must have same size as parents");
        assertEquals(new HashSet<>(locations), new HashSet<>(child.route), "All locations must be preserved");
    }

    @Test
    void testStartPreservedFromParent1() {
        List<Location> locations = generateLocations(4);

        Individual parent1 = new Individual(new ArrayList<>(locations), true);
        Individual parent2 = new Individual(Arrays.asList(
                locations.get(0), locations.get(2), locations.get(3), locations.get(1)
        ), true);

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 0.0, 2, 2, 0, 789, locations, null, true
        );

        Individual child = ga.crossover(parent1, parent2);

        assertEquals(parent1.route.get(0), child.route.get(0), "Child must start with parent1’s index 0 location");
    }

    @Test
    void testCrossoverScalesToLargeInputs() {
        List<Location> locations = generateLocations(100);

        List<Location> parent1Route = new ArrayList<>(locations);
        List<Location> parent2Route = new ArrayList<>(locations);
        Collections.shuffle(parent2Route, new Random(999));

        Individual parent1 = new Individual(parent1Route, true);
        Individual parent2 = new Individual(parent2Route, true);

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 0.0, 2, 2, 0, 999, locations, null, true
        );

        Individual child = ga.crossover(parent1, parent2);

        assertEquals(100, child.route.size());
        assertEquals(new HashSet<>(locations), new HashSet<>(child.route), "All locations must appear exactly once");
    }
}
