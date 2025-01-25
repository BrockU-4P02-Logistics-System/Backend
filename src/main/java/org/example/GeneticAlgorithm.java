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
}
