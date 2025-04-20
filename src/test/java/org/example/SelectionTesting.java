package org.example;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class SelectionTesting {

    private List<Location> generateLocations(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new Location(-80 + (i % 160), -160 + (i % 320), i + 1))
                .collect(Collectors.toList());
    }

    private Individual makeMockIndividual(double fitness, List<Location> route) {
        Individual ind = new Individual(route, true);
        ind.fitness = fitness;
        return ind;
    }

    @Test
    void testTournamentSelectionReturnsCorrectSize() {
        List<Location> locs = generateLocations(5);
        List<Individual> pop = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            pop.add(makeMockIndividual(i, locs));
        }

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 0.0, 2, 10, 0, 42, locs, null, true
        );
        ga.population = pop;

        List<Individual> selected = ga.tournamentSelection(3);
        assertEquals(10, selected.size(), "Selection should produce same number as population");
    }

    @Test
    void testTournamentSelectionNeverReturnsNull() {
        List<Location> locs = generateLocations(3);
        List<Individual> pop = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            pop.add(makeMockIndividual(i * 10.0, locs));
        }

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 0.0, 2, 5, 0, 42, locs, null, true
        );
        ga.population = pop;

        List<Individual> selected = ga.tournamentSelection(2);
        assertTrue(selected.stream().noneMatch(Objects::isNull), "No null individuals should be selected");
    }

    @Test
    void testHigherFitnessLessLikelyToBeSelected() {
        List<Location> locs = generateLocations(3);
        List<Individual> pop = new ArrayList<>();
        // Lower fitness is better in this setup
        pop.add(makeMockIndividual(1.0, locs)); // best
        pop.add(makeMockIndividual(10.0, locs));
        pop.add(makeMockIndividual(100.0, locs));
        pop.add(makeMockIndividual(200.0, locs));
        pop.add(makeMockIndividual(300.0, locs)); // worst

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(
                1, 1.0, 0.0, 2, 5, 0, 99, locs, null, true
        );
        ga.population = pop;

        Map<Individual, Integer> frequency = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            Individual selected = ga.tournamentProbability(3, 0.9);
            frequency.merge(selected, 1, Integer::sum);
        }

        // Sort by fitness ascending (best first)
        List<Individual> ordered = pop.stream()
                .sorted(Comparator.comparingDouble(Individual::getFitness))
                .collect(Collectors.toList());

        for (int i = 0; i < ordered.size() - 1; i++) {
            double freq1 = frequency.getOrDefault(ordered.get(i), 0);
            double freq2 = frequency.getOrDefault(ordered.get(i + 1), 0);
            assertTrue(freq1 >= freq2, "Better individuals should be selected more frequently");
        }
    }

    @Test
    void testSelectionIsReproducible() {
        List<Location> locs = generateLocations(3);
        List<Individual> pop = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            pop.add(makeMockIndividual(i, locs));
        }

        GeneticAlgorithm2 ga1 = new GeneticAlgorithm2(1, 1.0, 0.0, 2, 5, 0, 42, locs, null, true);
        GeneticAlgorithm2 ga2 = new GeneticAlgorithm2(1, 1.0, 0.0, 2, 5, 0, 42, locs, null, true);

        ga1.population = pop;
        ga2.population = pop;

        List<Individual> sel1 = ga1.tournamentSelection(2);
        List<Individual> sel2 = ga2.tournamentSelection(2);

        for (int i = 0; i < sel1.size(); i++) {
            assertEquals(sel1.get(i), sel2.get(i), "Same seed should give same selection order");
        }
    }
}
