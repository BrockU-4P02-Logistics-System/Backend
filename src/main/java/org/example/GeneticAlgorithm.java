package org.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.util.*;

public class GeneticAlgorithm {

    int stops;
    GraphHopper hopper;
    Map<String, Long> cache;

    GeneticAlgorithm(int s)
    {
        this.stops = s;
        cache = new HashMap<>();

        hopper = new GraphHopper();
        hopper.setOSMFile("ontario.osm.pbf");
        hopper.setGraphHopperLocation("graph-cache");

        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest"));
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));

        hopper.importOrLoad();
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

        for(int i = 0; i < subject.size() - 1; i++)
        {
            String key = subject.get(i).getID() + "-" + subject.get(i + 1).getID();

            if(!cache.containsKey(key))
            {
                GHRequest request = new GHRequest(subject.get(i).getLat(), subject.get(i).getLon(), subject.get(i+1).getLat(), subject.get(i+1).getLon()).setProfile("car").setLocale("en");

                GHResponse response = hopper.route(request);

                ResponsePath path = response.getBest();
                cache.put(key,path.getTime());
            }

            time = time + cache.get(key);
        }
        return 1 / time;
    }

    double[] evaluatePopulation(List<List<Location>> population)
    {
        double[] score = new double[population.size()];

        for(int i = 0; i < population.size(); i++)
        {
            double d = fitness(population.get(i));
            score[i] = d;
        }
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
}
