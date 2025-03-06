package org.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.core.Instance;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class PrecomputedDistance {
    public ConcurrentHashMap<String, Long> cache;
    public int[][] distanceMatrix;
    // Constructor: Stores a reference to the precomputed n×n distance matrix
    public PrecomputedDistance(List<Location> locations, GraphHopper hopper) {
        int n = locations.size();
        this.cache = new ConcurrentHashMap<>((n*n)-n);
        this.distanceMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                    continue;
                }
                distanceMatrix[i][j] = computeDistance(locations.get(i), locations.get(j), hopper);
            }
        }
    }

    public int computeDistance(Location a, Location b, GraphHopper hopper) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(a.getID());
        keyBuilder.append("-");
        keyBuilder.append(b.getID());
        String key = keyBuilder.toString();
        if(cache.containsKey(key)) return cache.get(key).intValue();
        if (a.getID() == b.getID()) return 0;
        GHRequest req = new GHRequest(a.getLat(), a.getLon(), b.getLat(), b.getLon());
        req.setProfile("car");
        GHResponse res = hopper.route(req);
        ResponsePath path = res.getBest();
        cache.put(key, path.getTime() / 60000);
        return (int) path.getTime();
    }

}
