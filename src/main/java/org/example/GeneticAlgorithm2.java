package org.example;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GeneticAlgorithm2 {
    graphHopperInitializer initializer;
    ConcurrentHashMap<String, Long> cache;
    List<Individual> population;
    List<Double> populationMin;
    List<Double> populationAverage;
    int numberIterations;
    double crossoverRate;
    double mutationRate;
    int tournamentSize;
    Random randomGen;
    List<Location> locations;
    int populationSize;
    Individual bestIndividual;
    double selectionPressure = 0.9;
    int[] generation;
    Individual[] elite;
    int eliteSize;
    boolean returnToStart;

    public List<Individual> initializePopulation(int size)
    {
        List<Individual> pop = new ArrayList<>(size);
        for (int i=0; i<size; i++)
        {
            int index = 0;
            List<Location> route = new ArrayList<>(this.locations);
            Collections.shuffle(route, this.randomGen);
            if(route.get(0).getID() != 1)
            {
                index = findIDOne(route);
            }
            Collections.swap(route,0,index);
            pop.add(new Individual(route, returnToStart));
        }
        return pop;
    }

    int findIDOne(List<Location> route)
    {
        for(int i = 0; i < route.size(); i++)
        {
            if (route.get(i).getID() == 1)
            {
                return i;
            }
        }
        return 0;
    }

    public List<Individual> tournamentSelection(int k)
    {
        List<Individual> newPop = new ArrayList<>(populationSize);
        for (int i=0; i<populationSize; i++)
        {
            newPop.add(tournamentProbability(k, this.selectionPressure));
        }
        return newPop;
    }

    public Individual tournamentProbability(int k, double probability)
    {
        List<Individual> tournament = new ArrayList<>();
        Random r = this.randomGen;

        for(int i = 0; i < k; i++)
        {
            while(true)
            {
                int s = r.nextInt(this.populationSize);
                if(!tournament.contains(this.population.get(s)))
                {
                    tournament.add(this.population.get(s));
                    break;
                }
            }
        }

        tournament = order(tournament);

        double role = r.nextDouble();
        double cumulative = probability;
        for(int i = 0; i < tournament.size(); i++)
        {
            if(role < cumulative)
            {
                return tournament.get(i);
            }
            else
            {
                cumulative += probability * Math.pow((1 - probability), i + 1);
            }
        }
        return tournament.get(0);
    }

    List<Individual> order(List<Individual> unordered)
    {
        unordered.sort(Comparator.comparingDouble(Individual::getFitness));
        return unordered;
    }

    public Individual tournament(int k)
    {
        List<Individual> tournament = new ArrayList<>(k);
        for (int i=0; i<k; i++)
        {
            int index = (int) (this.randomGen.nextDouble() * this.population.size());
            tournament.add(this.population.get(index));
        }
        return Collections.min(tournament, Comparator.comparingDouble(Individual::getFitness));
    }

    public List<Individual> crossoverPopulation(List<Individual> population){
        List<Individual> newPop = new ArrayList<>(populationSize);
        for (int i = 0; i<populationSize-1; i++){
            if(this.randomGen.nextDouble() < this.crossoverRate){
                Individual parent1 = population.get(i);
                Individual parent2 = population.get(i+1);
                newPop.add(crossover(parent1, parent2));
            }
            else{
                newPop.add(population.get(i));
            }
        }
        if (this.randomGen.nextDouble() < this.crossoverRate){
            //last and first
            Individual parent1 = population.get(populationSize-1);
            Individual parent2 = population.get(0);
            newPop.add(crossover(parent1, parent2));
        }
        else{
            newPop.add(population.get(populationSize-1));
        }
        return newPop;
    }

    public Individual crossover(Individual parent1, Individual parent2) {
        for(int i=0; i<parent1.route.size(); i++){
            if(parent1.route.get(i).getID() != parent2.route.get(i).getID()){
                break;
            }
            if(i == parent1.route.size()-1){
                return new Individual(parent1.route, returnToStart);
            }
        }
        int s = parent1.route.size();

        // Create a child list pre-filled with nulls.
        List<Location> child1 = new ArrayList<>(Collections.nCopies(s, null));
        Set<Location> used = new HashSet<>(); // Track locations already added

        // Fix index 0: copy it from parent1 (and mark it as used)
        child1.set(0, parent1.route.get(0));
        used.add(parent1.route.get(0));

        // Choose crossover points in the range [1, s-1] so index 0 is not selected.
        int i1 = this.randomGen.nextInt(s - 1) + 1;
        int i2 = this.randomGen.nextInt(s - 1) + 1;
        while (i1 == i2) {
            i2 = this.randomGen.nextInt(s - 1) + 1;
        }
        if (i1 > i2) {
            int temp = i1;
            i1 = i2;
            i2 = temp;
        }

        // Copy the slice from parent1 (from i1 to i2) into the child.
        for (int i = i1; i <= i2; i++) {
            Location loc = parent1.route.get(i);
            child1.set(i, loc);
            used.add(loc);
        }

        // Fill the remaining positions with locations from parent2 in order.
        // We fill only indices 1 to s-1. We start from index (i2 + 1) but ensure we skip index 0.
        int currentIndex = i2 + 1;
        if (currentIndex >= s) {
            currentIndex = 1;
        }
        for (Location loc : parent2.route) {
            if (!used.contains(loc)) {
                child1.set(currentIndex, loc);
                used.add(loc);
                currentIndex++;
                // Wrap around while ensuring index 0 is skipped.
                if (currentIndex >= s) {
                    currentIndex = 1;
                }
            }
        }
        return new Individual(child1, returnToStart);
    }


    public void evaluatePopulation(){
        double total = 0;
        double min = Double.MAX_VALUE;
        for (int i=0; i<populationSize; i++){
            total += population.get(i).calculateFitness(cache, initializer);
            if (population.get(i).getFitness() < min){
                min = population.get(i).getFitness();
            }
            if (bestIndividual == null || population.get(i).getFitness() < bestIndividual.getFitness()) {
                bestIndividual = population.get(i);
            }
        }
        this.populationMin.add(min);
        this.populationAverage.add(total/populationSize);
        if (this.elite == null) this.elite = new Individual[eliteSize];
        //Get elites
        Individual[] temp = new Individual[populationSize];
        for (int i=0; i<populationSize; i++){
            temp[i] = population.get(i);
        }
        Arrays.sort(temp, Comparator.comparingDouble(Individual::getFitness));
        if (eliteSize >= 0) System.arraycopy(temp, 0, this.elite, 0, eliteSize);
    }

    public List<Individual> mutatePopulation(List<Individual> population){
        List<Individual> newPop = new ArrayList<>(populationSize);
        for (int i=0; i<populationSize; i++){
            newPop.add(mutate(population.get(i)));
        }
        return newPop;
    }

    public Individual mutate(Individual individual){
        if(individual.route.size() <= 2){
            return individual;
        }
        List<Location> newRoute = new ArrayList<>(individual.route);
        int s = newRoute.size();
        if (this.randomGen.nextDouble() < this.mutationRate){
            int i1 = this.randomGen.nextInt(s - 1) + 1;
            int i2 = this.randomGen.nextInt(s - 1) + 1;
            while (i1 == i2){
                i2 = this.randomGen.nextInt(s - 1) + 1;
            }
            Collections.swap(newRoute, i1, i2);
        }
        return new Individual(newRoute, returnToStart);
    }

    public Individual mainLoop(){
        this.population = initializePopulation(this.populationSize);
        generation[0] = 0;
        for (int i=0; i<numberIterations; i++){
            generation[i+1] = i+1;
            if (elite!= null) this.population = addElite(this.population, elite);
            evaluatePopulation();
            System.out.println("Iteration: " + i + " Min: " + this.populationMin.get(i) + " Average: " + this.populationAverage.get(i));
            this.population = tournamentSelection(this.tournamentSize);
            this.population = crossoverPopulation(this.population);
            this.population = mutatePopulation(this.population);
        }
        evaluatePopulation();

        // Output and Graphing capabilities below

        //System.out.println("Iteration: " + numberIterations + " Min: " + this.populationMin.get(numberIterations) + " Average: " + this.populationAverage.get(numberIterations));
        //graphing();

        return this.bestIndividual;
    }

    public void graphing()
    {
        XYChart avgChart = new XYChartBuilder().width(800).height(300)
                .title("GA Average Fitness Over Generations")
                .xAxisTitle("Generation")
                .yAxisTitle("Average Fitness")
                .build();

        // Add fitness progression line
        XYSeries series = avgChart.addSeries("Average Fitness", Arrays.stream(this.generation).boxed().collect(Collectors.toList()), this.populationAverage);
        series.setMarker(SeriesMarkers.CIRCLE);  // Set point markers

        XYChart bestChart = new XYChartBuilder().width(800).height(300)  // Smaller height
                .title("GA Best Fitness Over Generations")
                .xAxisTitle("Generation")
                .yAxisTitle("Best Fitness")
                .build();
        XYSeries bestSeries = bestChart.addSeries("Best Fitness", Arrays.stream(this.generation).boxed().collect(Collectors.toList()), this.populationMin);
        bestSeries.setMarker(SeriesMarkers.CIRCLE);

        // === Display Both Charts in a Panel ===
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));  // 2 Rows, 1 Column
        panel.add(new XChartPanel<>(avgChart));  // Top Chart
        panel.add(new XChartPanel<>(bestChart)); // Bottom Chart

        // === Create Frame ===
        JFrame frame = new JFrame("GA Fitness Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public List<Individual> addElite(List<Individual> population, Individual[] elite){
        List<Individual> newPop = new ArrayList<>(populationSize);
        Set<Integer> indices = new HashSet<>();
        Random r = this.randomGen;
        while (indices.size() < elite.length){
            indices.add(r.nextInt(populationSize));
        }
        int i = 0;
        for (int j=0; j<populationSize; j++){
            if (indices.contains(j)){
                newPop.add(elite[i]);
                i++;
            }
            else{
                newPop.add(population.get(j));
            }
        }
        return newPop;
    }

    void printPopulation()
    {
        for(int i = 0; i < this.population.size(); i++)
        {
            System.out.println(this.population.get(i).route);
        }
    }

    public GeneticAlgorithm2(int iterations, double crossoverRate, double mutationRate, int tournamentSize, int populationSize, int eliteSize, int seed, List<Location> locations, graphHopperInitializer initializer, boolean returnToStart){
        this.cache = new ConcurrentHashMap<>();
        this.initializer = initializer;
        this.numberIterations = iterations;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.tournamentSize = tournamentSize;
        this.populationSize = populationSize;
        this.randomGen = new Random(seed);
        this.locations = locations;
        this.populationMin = new ArrayList<>(numberIterations+1);
        this.populationAverage = new ArrayList<>(numberIterations+1);
        this.bestIndividual = null;
        generation = new int[numberIterations+1];
        this.eliteSize = eliteSize;
        this.elite = null;
        this.returnToStart = returnToStart;
    }

}