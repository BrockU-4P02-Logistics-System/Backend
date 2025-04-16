package org.example;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class MutationTesting {

    // Reusable helper to generate valid locations
    private List<Location> generateLocations(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new Location(-90 + (i % 180), -180 + (i % 360), i + 1))
                .toList();
    }

    @Test
    void testMutationDoesNotChangeShortRoutes() {
        List<Location> locations = generateLocations(2);
        Individual individual = new Individual(locations, true);

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 1.0, 2, 2, 0, 42, locations, null, true
        );

        Individual mutated = ga.mutate(individual);
        assertEquals(individual.route, mutated.route, "Routes with size <= 2 should not mutate");
    }

    @Test
    void testMutationSwapsTwoDifferentLocations() {
        List<Location> locations = generateLocations(5);
        Individual original = new Individual(locations, true);

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 1.0, 2, 2, 0, 123, locations, null, true
        );

        Individual mutated = ga.mutate(original);

        assertEquals(new HashSet<>(original.route), new HashSet<>(mutated.route), "Mutated route must contain same locations");
        assertNotEquals(original.route, mutated.route, "Routes should differ after mutation");
        assertEquals(original.route.get(0), mutated.route.get(0), "Index 0 (depot) should not change");
    }

    @Test
    void testMutationProbabilityZeroMeansNoChange() {
        List<Location> locations = generateLocations(6);
        Individual individual = new Individual(locations, true);

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 0.0, 2, 2, 0, 42, locations, null, true
        );

        Individual mutated = ga.mutate(individual);
        assertEquals(individual.route, mutated.route, "Mutation rate 0 should not mutate");
    }

    @Test
    void testMutationIsReproducibleWithSameSeed() {
        List<Location> locations = generateLocations(10);
        Individual individual = new Individual(locations, true);

        GeneticAlgorithm2 ga1 = new GeneticAlgorithm2(
                1, 1.0, 1.0, 2, 2, 0, 999, locations, null, true
        );
        GeneticAlgorithm2 ga2 = new GeneticAlgorithm2(
                1, 1.0, 1.0, 2, 2, 0, 999, locations, null, true
        );

        Individual m1 = ga1.mutate(individual);
        Individual m2 = ga2.mutate(individual);

        assertEquals(m1.route, m2.route, "Same seed should produce identical mutation");
    }
}
