package org.example;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for utilization of GA, using standardized information passing
 */
public class TSPsolve
{

    int numberDrivers;
    boolean returnToStart;
    GraphHopper hopper;

    TSPsolve() throws IOException
    {
        Reader inputReader = new Reader("src/main/java/org/example/input.txt");

        this.numberDrivers = inputReader.numberDrivers;
        this.returnToStart = inputReader.returnToStart;
        boolean[] options = {false, false, false, false, false};
        graphHopperInitializer initializer = new graphHopperInitializer(options);
        // Initialize GraphHopper
        this.hopper = new GraphHopper();
        this.hopper.setOSMFile("ontario.osm.pbf");
        this.hopper.setGraphHopperLocation("graph-cache");
        this.hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest"));
        this.hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));
        this.hopper.importOrLoad();

        System.out.println("here");
        System.out.println(this.numberDrivers);

        VehicleRouter vr = new VehicleRouter(inputReader.locations, initializer, this.numberDrivers, options);
        vr.solveTSP(20, "src/main/java/org/example/output.txt");
    }

    public static void main (String [] args) throws IOException {
        TSPsolve t = new TSPsolve();
    }


}
