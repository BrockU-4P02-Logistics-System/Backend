package org.example;

import com.google.protobuf.Duration;
import com.graphhopper.GraphHopper;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VehicleRouter {
    private final List<Location> locations;
    private final int[][] distanceMatrix;
    private final int num_vehicles;

    public VehicleRouter(List<Location> locations, GraphHopper hopper, int num_vehicles) {
        this.locations = locations;
        this.num_vehicles = num_vehicles;

        // Use the PrecomputedDistance class
        PrecomputedDistance distances = new PrecomputedDistance(locations, hopper);
        this.distanceMatrix = distances.distanceMatrix;
    }

    public void solveTSP(int timeLimit, String outputGeoJsonFilePath) {
        Loader.loadNativeLibraries(); // Load OR-Tools

        int numLocations = distanceMatrix.length;
        System.out.println("Solving for " + numLocations + " locations with " + num_vehicles + " vehicles");

        RoutingIndexManager manager = new RoutingIndexManager(numLocations, num_vehicles, 0);
        RoutingModel routing = new RoutingModel(manager);

        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode((int) fromIndex);
            int toNode = manager.indexToNode((int) toIndex);
            return distanceMatrix[fromNode][toNode];
        });

        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);
        routing.addDimension(transitCallbackIndex, 0, 10000000, true, "Distance");
        RoutingDimension distanceDimension = routing.getMutableDimension("Distance");
        distanceDimension.setGlobalSpanCostCoefficient(100);

        RoutingSearchParameters searchParameters =
                main.defaultRoutingSearchParameters()
                        .toBuilder()
                        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                        .setTimeLimit(com.google.protobuf.Duration.newBuilder().setSeconds(timeLimit).build())
                        .build();

        Assignment solution = routing.solveWithParameters(searchParameters);

        if (solution != null) {
            List<Location> finalRoute = new ArrayList<>();

            // Process each vehicle's route
            for (int vehicle = 0; vehicle < num_vehicles; vehicle++) {
                System.out.print("Route for Vehicle " + vehicle + ": ");
                long index = routing.start(vehicle);
                StringBuilder routeStr = new StringBuilder();
                boolean firstNode = true; // flag for depot (first node)

                while (!routing.isEnd(index)) {
                    int routeIndex = manager.indexToNode((int) index);
                    // Get the original location from your list
                    Location originalLocation = locations.get(routeIndex);
                    // Create a new Location instance (to avoid modifying the shared object)
                    Location loc = new Location(originalLocation.getLat(), originalLocation.getLon(), originalLocation.id);

                    loc.clusterid = vehicle;

                    finalRoute.add(loc);
                    routeStr.append(routeIndex + 1).append(" -> ");
                    index = solution.value(routing.nextVar(index));
                }
                // Add the depot at the end as well, making a new copy with driver id 0.
                int depotIndex = manager.indexToNode((int) index);
                Location originalDepot = locations.get(depotIndex);
                Location depot = new Location(originalDepot.getLat(), originalDepot.getLon(), originalDepot.id);
                depot.clusterid = vehicle;
                finalRoute.add(depot);
                routeStr.append(depotIndex + 1);
                System.out.println(routeStr.toString());
            }


            try {
                new Route(finalRoute, outputGeoJsonFilePath);
                System.out.println("GeoJSON route output written to " + outputGeoJsonFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No solution found!");
        }
    }

}
