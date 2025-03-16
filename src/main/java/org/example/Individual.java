package org.example;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Individual {
    List<Location> route;
    double fitness;
    boolean returnToStart;
    public Individual(List<Location> route, boolean returnToStart)
    {
        this.route = route;
        this.fitness = Double.MAX_VALUE;
        this.returnToStart = returnToStart;
    }

    public double calculateFitness(ConcurrentHashMap<String, Long> cache, graphHopperInitializer initializer)
    {

        double time = 0;

        if (this.returnToStart)
        {
            //Calculate the fitness of the individual, this is the case when the driver is returning, so add on extra segment
            for(int i = 0; i < route.size(); i++)
            {
                String key = route.get(i % route.size()).getID() + "-" + route.get((i+1) % route.size()).getID();
                if(cache.containsKey(key))
                {
                    time += cache.get(key);
                }
                else
                {
                    GHRequest req = new GHRequest(route.get(i % route.size()).getLat(), route.get(i % route.size()).getLon(), route.get((i+1) % route.size()).getLat(), route.get((i+1) % route.size()).getLon())
                            .setProfile(initializer.generateProfileName(initializer.options))
                            .putHint("custom_model", graphHopperInitializer.getCustomModel(initializer.options));
                    GHResponse res = initializer.getHopper().route(req);
                    ResponsePath path = res.getBest();
                    time += path.getTime() / 60000;
                    cache.put(key, path.getTime() / 60000);
                }
            }
        }
        else
        {

            //Calculate the fitness of the individual, no returning, so normal calculation
            for(int i = 0; i < route.size() - 1; i++)
            {
                String key = route.get(i).getID() + "-" + route.get(i+1).getID();
                if(cache.containsKey(key))
                {
                    time += cache.get(key);
                }
                else
                {
                    GHRequest req = new GHRequest(route.get(i).getLat(), route.get(i).getLon(), route.get(i+1).getLat(), route.get(i+1).getLon())
                            .setProfile(initializer.generateProfileName(initializer.options))
                            .putHint("custom_model", graphHopperInitializer.getCustomModel(initializer.options));
                    GHResponse res = initializer.getHopper().route(req);
                    ResponsePath path = res.getBest();
                    time += path.getTime() / 60000;
                    cache.put(key, path.getTime() / 60000);
                }
            }
        }

        this.fitness = time;
        return time;
    }

    public double getFitness(){
        return this.fitness;
    }

    public List<Location> getRoute(){
        return this.route;
    }
}
