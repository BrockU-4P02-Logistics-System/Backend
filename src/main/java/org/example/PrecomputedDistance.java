package org.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.core.Instance;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class PrecomputedDistance implements DistanceMeasure {
    public ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();
    // Constructor: Stores a reference to the precomputed n×n distance matrix
    public PrecomputedDistance(List<Location> locations, GraphHopper hopper) {
        int n = locations.size();
        this.cache = new ConcurrentHashMap<>((n*n)-n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                computeDistance(locations.get(i), locations.get(j), hopper);
            }
        }
    }

    public void computeDistance(Location a, Location b, GraphHopper hopper) {
        String key = a.getID() + "-" + b.getID();
        if(cache.containsKey(key) || a.getID() == b.getID())
        {
            return;
        }
        GHRequest req = new GHRequest(a.getLat(), a.getLon(), b.getLat(), b.getLon());
        req.setProfile("car");
        GHResponse res = hopper.route(req);
        ResponsePath path = res.getBest();
        cache.put(key, path.getTime() / 60000);
    }

    @Override
    public double measure(Instance a, Instance b) {
        // Extract location IDs (stored in Instance as a single feature)
        int idA = (int) a.value(0);
        int idB = (int) b.value(0);

        // Ensure correct ordering of the key
        String key = idA + "-" + idB;

        // Retrieve distance from cache, return a large value if not found
        return cache.getOrDefault(key, Long.MAX_VALUE);
    }

    @Override
    public double getMinValue() {
        return 0; // Minimum possible distance (self-distance)
    }

    @Override
    public double getMaxValue() {
        return Double.MAX_VALUE; // Large value to avoid overflow
    }

    @Override
    public boolean compare(double x, double y) {
        return x < y; // Standard comparison for minimization
    }
}
