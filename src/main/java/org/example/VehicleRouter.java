package org.example;

import com.google.protobuf.Duration;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;

import java.util.List;

public class VehicleRouter {
    private final int[][] distanceMatrix;
    private final int num_vehicles;

    public VehicleRouter(List<Location> locations, graphHopperInitializer initializer, int num_vehicles, boolean[] options) {
        this.num_vehicles = num_vehicles;

        // Use the PrecomputedDistance class
        PrecomputedDistance distances = new PrecomputedDistance(locations, initializer, options);
        this.distanceMatrix = distances.distanceMatrix;
    }

    public void solveTSP(int timeLimit) {
        Loader.loadNativeLibraries(); // Load OR-Tools
        int numLocations = distanceMatrix.length;
        System.out.println("Solving for " + numLocations + " locations with " + num_vehicles + " vehicles");

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
            // Print routes
            for (int i = 0; i < num_vehicles; i++) {
                System.out.print("Route for Vehicle " + (i) + ": ");
                long index = routing.start(i);
                StringBuilder route = new StringBuilder();
                while (!routing.isEnd(index)) {
                    int routeIndex = manager.indexToNode((int) index);
                    route.append(routeIndex+1).append(" -> ");
                    index = solution.value(routing.nextVar(index));
                }
                int finalIndex = manager.indexToNode((int) index);
                route.append(finalIndex + 1);
                System.out.println(route);
            }
        }
        else {
            System.out.println("No solution found!");
        }
    }
}