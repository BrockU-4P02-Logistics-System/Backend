package org.example;

import com.google.protobuf.Duration;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VehicleRouter {
    private final int[][] distanceMatrix;
    private final int num_vehicles;
    private final List<Location> locations;

    public VehicleRouter(List<Location> locations, graphHopperInitializer initializer, int num_vehicles, boolean[] options) {
        this.num_vehicles = num_vehicles;
        this.locations = locations;
        // Use the PrecomputedDistance class
        PrecomputedDistance distances = new PrecomputedDistance(locations, initializer, options);
        this.distanceMatrix = distances.distanceMatrix;
    }

    public void solveTSP(int timeLimit, String outputFileName) {
        Loader.loadNativeLibraries(); // Load OR-Tools
        int numLocations = distanceMatrix.length;
        //System.out.println("Solving for " + numLocations + " locations with " + num_vehicles + " vehicles");

        // Create routing index manager
        // For a multiple vehicle routing problem with a depot
        RoutingIndexManager manager = new RoutingIndexManager(numLocations, num_vehicles, 0);

        // Create routing model
        RoutingModel routing = new RoutingModel(manager);

        long[][] lookupDistanceMatrix = new long[numLocations][numLocations];
        for (int i = 0; i < numLocations; i++) {
            for (int j = 0; j < numLocations; j++) {
                lookupDistanceMatrix[i][j] = distanceMatrix[i][j];
            }
        }

        final int transitCallbackIndex = routing.registerTransitCallback((fromIndex, toIndex) ->
                lookupDistanceMatrix[manager.indexToNode((int) fromIndex)][manager.indexToNode((int) toIndex)]
        );


        // Define cost of each arc
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // Add Distance constraint
        routing.addDimension(transitCallbackIndex, 0, 10000000, true, "Distance");
        RoutingDimension distanceDimension = routing.getMutableDimension("Distance");
        distanceDimension.setGlobalSpanCostCoefficient(100);
        // Setting first solution heuristic
        RoutingSearchParameters searchParameters =
                main.defaultRoutingSearchParameters()
                        .toBuilder()
                        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.CHRISTOFIDES)
                        .setTimeLimit(Duration.newBuilder().setSeconds(timeLimit).build())
                        .build();
        // Solve the problem
        Assignment solution = routing.solveWithParameters(searchParameters);

        // Print solut  ion
        if (solution != null) {
            List<Location> finalRoute = new ArrayList<>();
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
            }
            try {
                new Route(finalRoute, outputFileName);
                System.out.println("GeoJSON route output written to " + outputFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No solution found!");
        }
    }
}