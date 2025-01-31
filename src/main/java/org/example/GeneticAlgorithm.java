package org.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import java.util.*;

public class GeneticAlgorithm {

    GraphHopper hopper;
    Map<String, Long> cache;
    List<List<Location>> population;
    double[] populationScore;
    double populationAverage;
    int numberIterations;
    double crossoverRate;
    double mutationRate;

    GeneticAlgorithm(List<Location> Locations)
    {
        cache = new HashMap<>();

        hopper = new GraphHopper();
        hopper.setOSMFile("ontario.osm.pbf");
        hopper.setGraphHopperLocation("graph-cache");

        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest"));
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));

        hopper.importOrLoad();

        this.population = initialPopulation(Locations,Locations.size() * Locations.size());
        this.populationScore = new double[Locations.size() * Locations.size()];
        this.numberIterations = 1000;
        this.mutationRate = 0.2;
        this.crossoverRate = 0.75;

        updateScore();
        for(int i = 0; i < this.numberIterations; i++)
        {
            for(int j = 0; j < this.population.size() * 0.1; j++)
            {
                replace();
            }
            System.out.println("Generation " + (i + 1) + ": Best Score = " + currBestScore());
        }
    }

    List<List<Location>> initialPopulation(List<Location> locations, int popSize)
    {
        List<List<Location>> init = new ArrayList<>();
        Random r = new Random();

        for(int i = 0; i < popSize; i++)
        {
            List<Location> route = new ArrayList<>(locations);
            Collections.shuffle(route, r);
            init.add(route);
        }
        return init;
    }

    double fitness(List<Location> subject)
    {
        double time = 0;

        for(int i = 0; i < subject.size(); i++)
        {
            String key = subject.get(i).getID() + "-" + subject.get((i + 1) % subject.size()).getID();

            if(!cache.containsKey(key))
            {
                GHRequest request = new GHRequest(subject.get(i).getLat(), subject.get(i).getLon(), subject.get((i + 1) % subject.size()).getLat(), subject.get((i + 1) % subject.size()).getLon()).setProfile("car").setLocale("en");

                GHResponse response = hopper.route(request);

                ResponsePath path = response.getBest();
                cache.put(key,path.getTime() / 60000);
            }

            time = time + cache.get(key);
        }
        return time; //Smaller time will give better score
    }

    double[] evaluatePopulation(List<List<Location>> population)
    {
        double[] score = new double[population.size()];
        double sum = 0;

        for(int i = 0; i < population.size(); i++)
        {
            double d = fitness(population.get(i));
            score[i] = d;
            sum = sum + d;
        }
        this.populationAverage = sum / population.size();
        return score;
    }

    List<Location> mutate(List<Location> input, double rate)
    {
        // This will mutate one route
        Random r = new Random();
        List<Location> m = new ArrayList<>(input);
        int s = m.size();

        if(r.nextDouble() < rate)
        {
            int i1 = r.nextInt(s);
            int i2 = r.nextInt(s);

            while(i1 == i2)
            {
                i2 = r.nextInt(m.size());
            }

            Collections.swap(m,i1,i2);
        }
        return m;
    }

    List<Location> orderedCrossover(List<Location> p1, List<Location> p2)
    {
        Random r = new Random();
        int s = p1.size();

        List<Location> child1 = new ArrayList<>(Collections.nCopies(s, null));

        int i1 = r.nextInt(s);
        int i2 = r.nextInt(s);

        while(i1 == i2)
        {
            i2 = r.nextInt(s);
        }

        if (i1 > i2) {
            int temp = i1;
            i1 = i2;
            i2 = temp;
        }

        for (int i = i1; i <= i2; i++) {
            child1.set(i, p1.get(i));
        }

        int currentIndex = (i2 + 1) % s;
        for (int i = 0; i < s; i++) {
            Location temp = p2.get((i2 + 1 + i) % s);
            if (!child1.contains(temp)) {
                child1.set(currentIndex, temp);
                currentIndex = (currentIndex + 1) % s;
            }
        }
        return child1;
    }

    List<List<Location>> getPopulation()
    {
        return this.population;
    }

    void printPopulation()
    {
        for(List<Location> element : this.population)
        {
            System.out.println(element);
        }
    }

    void updateScore()
    {
        this.populationScore = evaluatePopulation(this.population);
    }

    int currBestRouteIndex()
    {
        double best = Double.POSITIVE_INFINITY;
        int bestIn = 0;

        for(int i = 0; i < populationScore.length; i++)
        {
            if(populationScore[i] < best)
            {
                bestIn = i;
                best = populationScore[i];
            }
        }
        return bestIn;
    }

    List<Location> currBestRoute()
    {
        return this.population.get(currBestRouteIndex());
    }

    double currBestScore()
    {
        return this.populationScore[currBestRouteIndex()];
    }

    List<Location> parentSelection()
    {
        Random r = new Random();
        int ran = r.nextInt(this.population.size());
        return this.population.get(ran);
    }

    int findWeakIndex()
    {
        Random r = new Random();
        do
        {
            int index = r.nextInt(this.populationScore.length);

            if(this.populationScore[index] >= this.populationAverage)
            {
                return index;
            }
        } while (true);
    }

    void replace()
    {
        Random r = new Random();
        List<Location> parent1 = parentSelection();
        List<Location> parent2 = parentSelection();
        List<Location> child;
        do
        {
            if(parent1 != parent2)
            {
                break;
            }
            else
            {
                parent2 = parentSelection();
            }
        } while (true);

        double gen = r.nextDouble();
        if(gen < this.crossoverRate)
        {
            child = orderedCrossover(parent1,parent2);
        }
        else if(gen < (1 - this.crossoverRate) / 2)
        {
            child = parent1;
        }
        else
        {
            child = parent2;
        }

        child = mutate(child,this.mutationRate);
        int ran = r.nextInt(this.population.size());

        this.population.set(ran,child);

        updateScore();
    }

    public static void main(String [] args)
    {
        // Dummy data
        Location one = new Location(43.651070,-79.347015,1); //Toronto
        Location two = new Location(44.3894,-79.6903,2); //Barrie
        Location three = new Location(45.421532,-75.697189,3); //Ottawa
        Location four = new Location(43.1594,-79.2469,4); //St Catharines
        Location five = new Location(43.2557,-79.8711,5); //Hamilton
        Location six = new Location(43.7315,-79.7624,6); //Brampton
        Location seven = new Location(48.3809,-89.2477,7); //Thunder Bay

        List<Location> Locations = new ArrayList<>();
        Locations.add(one);
        Locations.add(two);
        Locations.add(three);
        Locations.add(four);
        Locations.add(five);
        Locations.add(six);
        Locations.add(seven);

        GeneticAlgorithm g = new GeneticAlgorithm(Locations);
    }
}
