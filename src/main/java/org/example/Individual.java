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

    public double calculateFitness(ConcurrentHashMap<String, Double> cache, graphHopperInitializer initializer) {
        double totalTime = 0.0;

        if (this.returnToStart) {
            int n = route.size();
            for (int i = 0; i < n; i++) {
                int nextIndex = (i + 1) % n;
                String key = route.get(i).getID() + "-" + route.get(nextIndex).getID();
                double segmentTime = 0.0;

                if (cache.containsKey(key)) {
                    segmentTime = cache.get(key);
                } else {
                    GHRequest req = new GHRequest(
                            route.get(i).getLat(), route.get(i).getLon(),
                            route.get(nextIndex).getLat(), route.get(nextIndex).getLon())
                            .setProfile(initializer.generateProfileName(initializer.options))
                            .putHint("custom_model", graphHopperInitializer.getCustomModel(initializer.options));
                    GHResponse res = initializer.getHopper().route(req);

                    if (res.hasErrors()) {
                        segmentTime = calculateEuclideanDistanceTime(
                                route.get(i).getLat(), route.get(i).getLon(),
                                route.get(nextIndex).getLat(), route.get(nextIndex).getLon());
                    } else {
                        ResponsePath path = res.getBest();
                        segmentTime = path.getTime() / 60000.0;
                    }
                    cache.put(key, segmentTime);
                }
                totalTime += segmentTime;
            }
        } else {
            for (int i = 0; i < route.size() - 1; i++) {
                String key = route.get(i).getID() + "-" + route.get(i + 1).getID();
                double segmentTime = 0.0;

                if (cache.containsKey(key)) {
                    segmentTime = cache.get(key);
                } else {
                    GHRequest req = new GHRequest(
                            route.get(i).getLat(), route.get(i).getLon(),
                            route.get(i + 1).getLat(), route.get(i + 1).getLon())
                            .setProfile(initializer.generateProfileName(initializer.options))
                            .putHint("custom_model", graphHopperInitializer.getCustomModel(initializer.options));
                    GHResponse res = initializer.getHopper().route(req);

                    if (res.hasErrors()) {
                        segmentTime = calculateEuclideanDistanceTime(
                                route.get(i).getLat(), route.get(i).getLon(),
                                route.get(i + 1).getLat(), route.get(i + 1).getLon());
                    } else {
                        ResponsePath path = res.getBest();
                        segmentTime = path.getTime() / 60000.0;
                    }
                    cache.put(key, segmentTime);
                }
                totalTime += segmentTime;
            }
        }

        this.fitness = totalTime;
        return totalTime;
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
        // Then divide by 600
        return ((distance / (50 * 1000 / 3600)) / 60);
    }

    public double getFitness(){
        return this.fitness;
    }

    public List<Location> getRoute(){
        return this.route;
    }
}
