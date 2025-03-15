package org.example;

import java.io.IOException;
/**
 * Class for utilization of GA, using standardized information passing
 */
public class TSPsolve
{

    int numberDrivers;
    boolean returnToStart;

    TSPsolve() throws IOException
    {
        Reader inputReader = new Reader("src/main/java/org/example/input.txt");
        this.numberDrivers = inputReader.numberDrivers;
        this.returnToStart = inputReader.returnToStart;
        boolean[] options = {false, false, false, false, false};
        graphHopperInitializer initializer = new graphHopperInitializer();
        initializer = new graphHopperInitializer(options);
        VehicleRouter vr = new VehicleRouter(inputReader.locations, initializer, this.numberDrivers, options);
        vr.solveTSP(20, "src/main/java/org/example/output.txt");
    }

    public static void main (String [] args) throws IOException {
        TSPsolve t = new TSPsolve();
    }


}
