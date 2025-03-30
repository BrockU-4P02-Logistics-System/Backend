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
                    if (res.hasErrors()) {

                        time += calculateEuclideanDistanceTime(route.get(i % route.size()).getLat(), route.get(i % route.size()).getLon(), route.get((i+1) % route.size()).getLat(),
                                route.get((i+1) % route.size()).getLon());
                        cache.put(key, (long) time);
                    }
                    else {
                        ResponsePath path = res.getBest();
                        time += path.getTime() / 60000;
                        cache.put(key, path.getTime() / 60000);
                    }
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
                    if (res.hasErrors()) {
                        time += calculateEuclideanDistanceTime(route.get(i).getLat(), route.get(i).getLon(), route.get(i+1).getLat(), route.get(i+1).getLon());
                        cache.put(key, (long) time);
                    }
                    else {
                        ResponsePath path = res.getBest();
                        time += path.getTime() / 60000;
                        cache.put(key, path.getTime() / 60000);
                    }
                }
            }
        }

        this.fitness = time;
        return time;
    }

    private double calculateEuclideanDistanceTime(double lat1, double lon1, double lat2, double lon2) {
        // Earth's radius in meters
        final double R = 6371000;

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double h = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);

        double distance = 2 * R * Math.asin(Math.sqrt(h));

        // Convert to an approximate time in seconds (assuming 50 km/h average speed)
        // Then divide by 6000 as in your original code
        return ((distance / (50 * 1000 / 3600)) / 600);
    }

    public double getFitness(){
        return this.fitness;
    }

    public List<Location> getRoute(){
        return this.route;
    }
}
