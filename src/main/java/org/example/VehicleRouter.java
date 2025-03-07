package org.example;

import com.google.protobuf.Duration;
import com.graphhopper.GraphHopper;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;

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

    public void solveTSP(int timeLimit) {
        Loader.loadNativeLibraries(); // Load OR-Tools

        int numLocations = distanceMatrix.length;
        System.out.println("Solving for " + numLocations + " locations with " + num_vehicles + " vehicles");

        // Create routing index manager
        // For a multiple vehicle routing problem with a depot
        RoutingIndexManager manager = new RoutingIndexManager(numLocations, num_vehicles, 0);

        // Create routing model
        RoutingModel routing = new RoutingModel(manager);

        // Create and register a transit callback
        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            // Convert from routing variable Index to user NodeIndex
            int fromNode = manager.indexToNode((int) fromIndex);
            int toNode = manager.indexToNode((int) toIndex);
            return distanceMatrix[fromNode][toNode];
        });

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
                        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
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