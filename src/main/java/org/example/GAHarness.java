package org.example;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GAHarness {


    public GAHarness(int numberDrivers) throws IOException
    {
        Reader newReader = new Reader("src/main/java/org/example/input.txt");
        boolean[] options = {false, false, false};

        graphHopperInitializer initializer = new graphHopperInitializer();
        initializer = new graphHopperInitializer(options);

        //testJavaML ml = new testJavaML();
        //ml.cluster(numberDrivers,10000,newReader.locations,hopper);
        for(int i = 0; i < numberDrivers; i++)
        {
            List<Location> clusteriLocations = new ArrayList<>();

            for(int j = 0; j < newReader.locations.size(); j++)
            {
                if(newReader.locations.get(j).getClusterid() == i || newReader.locations.get(j).getID() == 1)
                {
                    clusteriLocations.add(newReader.locations.get(j));
                }
            }


            System.out.println(clusteriLocations.size());
            if(clusteriLocations.size() > 1)
            {
                GeneticAlgorithm2 ga = new GeneticAlgorithm2(100, 0.75, 0.2, 3,
                        clusteriLocations.size() * clusteriLocations.size() + 10, 3, 42,
                        clusteriLocations, initializer, false);
                Individual bestIndividual = ga.mainLoop();
                Route r = new Route(bestIndividual.getRoute(), "src/main/java/org/example/output.txt", false);
                ga.printPopulation();
            }
        }
    }



    public static void main(String[] args) throws IOException {
        GAHarness gah = new GAHarness(1);
    }
}
