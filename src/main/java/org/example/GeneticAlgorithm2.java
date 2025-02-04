package org.example;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GeneticAlgorithm2 {
    GraphHopper hopper;
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

    public List<Individual> initializePopulation(int size){
        List<Individual> pop = new ArrayList<>(size);
        for (int i=0; i<size; i++){
            List<Location> route = new ArrayList<>(this.locations);
            Collections.shuffle(route, this.randomGen);
            pop.add(new Individual(route));
        }
        return pop;
    }

    public List<Individual> tournamentSelection(int k){
        List<Individual> newPop = new ArrayList<>(populationSize);
        for (int i=0; i<populationSize; i++){
            newPop.add(tournamentProbability(k, 0.9));
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
        return tournament.getFirst();
    }

    List<Individual> order(List<Individual> unordered)
    {
        unordered.sort(Comparator.comparingDouble(Individual::getFitness));
        return unordered;
    }

    public Individual tournament(int k){
        List<Individual> tournament = new ArrayList<>(k);
        for (int i=0; i<k; i++){
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

    public Individual crossover(Individual parent1, Individual parent2){
        int s = parent1.route.size();

        List<Location> child1 = new ArrayList<>(Collections.nCopies(s, null));
        Set<Location> used = new HashSet<>(); // Track used locations

        // Ensure distinct crossover points
        int i1 = this.randomGen.nextInt(s);
        int i2 = this.randomGen.nextInt(s);

        while (i1 == i2) {
            i2 = this.randomGen.nextInt(s);
        }

        if (i1 > i2) {
            int temp = i1;
            i1 = i2;
            i2 = temp;
        }

        // Copy slice from parent 1
        for (int i = i1; i <= i2; i++) {
            child1.set(i, parent1.route.get(i));
            used.add(parent1.route.get(i));
        }

        // Fill remaining spots from parent 2
        int currentIndex = (i2 + 1) % s;
        for (Location temp : parent2.route) {
            if (!used.contains(temp)) {
                child1.set(currentIndex, temp);
                used.add(temp);
                currentIndex = (currentIndex + 1) % s;
            }
        }
        return new Individual(child1);
    }

    public void evaluatePopulation(){
        double total = 0;
        double min = Double.MAX_VALUE;
        for (int i=0; i<populationSize; i++){
            total += population.get(i).calculateFitness(cache, hopper);
            if (population.get(i).getFitness() < min){
                min = population.get(i).getFitness();
            }
            if (bestIndividual == null || population.get(i).getFitness() < bestIndividual.getFitness()) {
                bestIndividual = population.get(i);
            }
        }
        this.populationMin.add(min);
        this.populationAverage.add(total/populationSize);
    }

    public List<Individual> mutatePopulation(List<Individual> population){
        List<Individual> newPop = new ArrayList<>(populationSize);
        for (int i=0; i<populationSize; i++){
            newPop.add(mutate(population.get(i)));
        }
        return newPop;
    }

    public Individual mutate(Individual individual){
        List<Location> newRoute = new ArrayList<>(individual.route);
        int s = newRoute.size();
        if (this.randomGen.nextDouble() < this.mutationRate){
            int i1 = this.randomGen.nextInt(s);
            int i2 = this.randomGen.nextInt(s);
            while (i1 == i2){
                i2 = this.randomGen.nextInt(s);
            }
            Collections.swap(newRoute, i1, i2);
        }
        return new Individual(newRoute);
    }

    public Individual mainLoop(){
        this.population = initializePopulation(this.populationSize);
        for (int i=0; i<numberIterations; i++){
            evaluatePopulation();
            System.out.println("Iteration: " + i + " Min: " + this.populationMin.get(i) + " Average: " + this.populationAverage.get(i));
            this.population = tournamentSelection(this.tournamentSize);
            this.population = crossoverPopulation(this.population);
            this.population = mutatePopulation(this.population);
        }
        evaluatePopulation();
        System.out.println("Iteration: " + numberIterations + " Min: " + this.populationMin.get(numberIterations) + " Average: " + this.populationAverage.get(numberIterations));
        return this.bestIndividual;
    }

    public GeneticAlgorithm2(int iterations, double crossoverRate, double mutationRate, int tournamentSize, int populationSize, int seed, List<Location> locations){
        this.cache = new ConcurrentHashMap<>();

        hopper = new GraphHopper();
        hopper.setOSMFile("ontario.osm.pbf");
        hopper.setGraphHopperLocation("graph-cache");
        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest"));
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));
        hopper.importOrLoad();

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
    }

}