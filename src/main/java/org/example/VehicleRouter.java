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

    public Route solveTSP(int timeLimit, String outputFileName, boolean returnToStart) {
        int numLocations = distanceMatrix.length;
        //System.out.println("Solving for " + numLocations + " locations with " + num_vehicles + " vehicles");

        // Create routing index manager
        // For a multiple vehicle routing problem with a depot
        int[] starts = new int[num_vehicles];
        int[] ends = new int[num_vehicles];
        for (int i = 0; i < num_vehicles; i++) {
            starts[i] = 0;
            // If returning to start, end at depot 0. Otherwise, the route is open;
            // we still specify depot 0 as the endpoint, but later override its cost.
            ends[i] = 0;
        }

        // Create a RoutingIndexManager with as many start/end indices as vehicles.
        RoutingIndexManager manager = new RoutingIndexManager(numLocations, num_vehicles, starts, ends);

        // Create the RoutingModel.
        RoutingModel routing = new RoutingModel(manager);

        // Copy your distance matrix into a lookup table.
        long[][] lookupDistanceMatrix = new long[numLocations][numLocations];
        for (int i = 0; i < numLocations; i++) {
            for (int j = 0; j < numLocations; j++) {
                lookupDistanceMatrix[i][j] = distanceMatrix[i][j];
            }
        }

        // Register a transit callback.
        // If returnToStart is false, we override the cost for the final leg (back to depot) to 0.
        final int transitCallbackIndex = routing.registerTransitCallback((fromIndex, toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            // Check if this arc is the final leg back to the depot.
            if (!returnToStart && toNode == 0 && fromNode != 0) {
                // For open routes, ignore the cost of returning to the depot.
                return 0L;
            }
            return lookupDistanceMatrix[fromNode][toNode];
        });

        // Define cost of each arc using the transit callback.
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // Add a distance dimension to constrain the distance.
        // The capacity and slack are set arbitrarily high here (adjust as needed).
        routing.addDimension(
                transitCallbackIndex,  // transit callback
                0,                     // no slack
                1000000000,            // maximum distance per route
                true,                  // start cumul at zero
                "Distance"
        );
        RoutingDimension distanceDimension = routing.getMutableDimension("Distance");
        distanceDimension.setGlobalSpanCostCoefficient(200);
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
            // Form this route based on google or tools
            List<Location> finalRoute = new ArrayList<>();

            for (int vehicle = 0; vehicle < num_vehicles; vehicle++) {
                // System.out.print("Route for Vehicle " + vehicle + ": ");
                long index = routing.start(vehicle);
                StringBuilder routeStr = new StringBuilder();

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

                if(returnToStart) // Add the starting location to end of drivers route for all drivers
                {
                    Location loc = new Location(locations.get(0).getLat(), locations.get(0).getLon(), locations.get(0).id);
                    loc.clusterid = vehicle;
                    finalRoute.add(loc);
                }

            }
            try {
                System.out.println("Solution found!");
                Route r = new Route(finalRoute, outputFileName, false); // Always pass false to Route, the logic for returning to the start is in the previous loop
                System.out.println("GeoJSON route output written to " + outputFileName);
                return r;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No solution found!");
        }
        return null;
    }
}