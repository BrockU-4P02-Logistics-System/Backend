package org.example;

import com.google.ortools.Loader;
import java.io.IOException;
import java.util.List;

public class EntryPoint {
    graphHopperInitializer initializer;
    public EntryPoint(){
        System.gc();
        this.initializer = new graphHopperInitializer();
        System.gc();
        Loader.loadNativeLibraries(); // Load OR-Tools
    }
    public void spawnWorker(List<Location> locations, boolean[] options, int num_vehicles, boolean returnToStart) throws IOException {
        if (num_vehicles == 1){
            GeneticAlgorithm2 ga = new GeneticAlgorithm2(100, 0.75, 0.2, 3,
                    locations.size() * locations.size() + 10, 3, 42,
                    locations, initializer, returnToStart);
            Individual bestIndividual = ga.mainLoop();
            Route r = new Route(bestIndividual.getRoute(), "src/main/java/org/example/output.txt", returnToStart);
        } else {
            VehicleRouter vr = new VehicleRouter(locations, initializer, num_vehicles, options);
            Route r = vr.solveTSP(20, "src/main/java/org/example/output.txt", returnToStart); //could be null if no solution found
        }
    }
    public static void main(String[] args) throws IOException
    { // Example usage
        EntryPoint ep = new EntryPoint();
        Reader inputReader = new Reader("src/main/java/org/example/input.txt");

        // Transfer reader data
        boolean[] options = inputReader.flags; // Avoid highways, avoid tolls, avoid unpaved roads, avoid ferries, avoid tracks
        int num_vehicles = inputReader.numberDrivers;
        List<Location> locations = inputReader.locations;
        boolean returnToStart = inputReader.returnToStart;

        // solve problem
        ep.spawnWorker(locations, options, num_vehicles, returnToStart);
        //ep.spawnWorker(locations, new boolean[]{true, false, false, false, false}, num_vehicles); //Just did this as a test. It's like instant after the first one
    }

}
