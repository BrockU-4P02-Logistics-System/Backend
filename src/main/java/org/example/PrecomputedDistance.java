package org.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.DefaultSnapFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.shapes.GHPoint;

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
        System.out.println("Precomputed distance matrix:");
    }



    public int computeDistance(Location a, Location b) {
        if (a.getID() == b.getID()) return 0;

        GraphHopper hopper = initializer.getHopper();
        String profileName = initializer.generateProfileName(options);

        // Create a simple request without any special configuration
        GHRequest req = new GHRequest()
                .addPoint(new GHPoint(a.getLat(), a.getLon()))
                .addPoint(new GHPoint(b.getLat(), b.getLon()))
                .setProfile(profileName);

        GHResponse res = hopper.route(req);

        if (res.hasErrors()) {
            return calculateEuclideanDistanceTime(a, b);
        }

        ResponsePath path = res.getBest();
        return (int) (path.getTime() / 6000);
    }

    private int calculateEuclideanDistanceTime(Location a, Location b) {
        // Earth's radius in meters
        final double R = 6371000;

        double lat1 = Math.toRadians(a.getLat());
        double lon1 = Math.toRadians(a.getLon());
        double lat2 = Math.toRadians(b.getLat());
        double lon2 = Math.toRadians(b.getLon());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double h = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);

        double distance = 2 * R * Math.asin(Math.sqrt(h));

        // Convert to an approximate time in seconds (assuming 50 km/h average speed)
        // Then divide by 6000 as in your original code
        return (int) ((distance / (50 * 1000 / 3600)) / 60);
    }


}
