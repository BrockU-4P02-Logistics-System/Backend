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
    public Individual(List<Location> route)
    {
        this.route = route;
        this.fitness = Double.MAX_VALUE;
    }
    public double calculateFitness(ConcurrentHashMap<String, Long> cache, GraphHopper hopper)
    {
        //Calculate the fitness of the individual
        double time = 0;
        for(int i = 0; i < route.size() - 1; i++)
        {
            String key = route.get(i).getID() + "-" + route.get(i+1).getID();
            if(cache.containsKey(key))
            {
                time += cache.get(key);
            }
            else
            {
                GHRequest req = new GHRequest(route.get(i).getLat(), route.get(i).getLon(), route.get(i+1).getLat(), route.get(i+1).getLon());
                req.setProfile("car");
                GHResponse res = hopper.route(req);
                ResponsePath path = res.getBest();
                time += path.getTime() / 60000;
                cache.put(key, path.getTime() / 60000);
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
