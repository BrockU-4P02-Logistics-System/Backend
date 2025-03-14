package org.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;

import java.util.List;

public class PrecomputedDistance {
    public int[][] distanceMatrix;
    graphHopperInitializer initializer;
    boolean[] options;
    // Constructor: Stores a reference to the precomputed n×n distance matrix
    public PrecomputedDistance(List<Location> locations, graphHopperInitializer initializer, boolean[] options) {
        int n = locations.size();
        this.initializer = initializer;
        this.options = options;
        this.distanceMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) distanceMatrix[i][j] = 0;
                else distanceMatrix[i][j] = computeDistance(locations.get(i), locations.get(j));
            }
        }
    }

    public int computeDistance(Location a, Location b) {
        if (a.getID() == b.getID()) return 0;
        GHRequest req = new GHRequest(a.getLat(), a.getLon(), b.getLat(), b.getLon())
                .setProfile(initializer.generateProfileName(options))
                .putHint("custom_model", graphHopperInitializer.getCustomModel(options));
        GHResponse res = initializer.getHopper().route(req);
        ResponsePath path = res.getBest();
        return (int) path.getTime();
    }

}
