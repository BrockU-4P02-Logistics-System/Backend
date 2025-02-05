package org.example;

import java.util.ArrayList;
import java.util.List;

public class GAHarness {
    public static void main(String[] args) {

        // Dummy data
        Location one = new Location(43.651070,-79.347015,1); //Toronto
        Location two = new Location(44.3894,-79.6903,2); //Barrie
        Location three = new Location(45.421532,-75.697189,3); //Ottawa
        Location four = new Location(43.1594,-79.2469,4); //St Catharines
        Location five = new Location(43.2557,-79.8711,5); //Hamilton
        Location six = new Location(43.7315,-79.7624,6); //Brampton
        Location seven = new Location(48.3809,-89.2477,7); //Thunder Bay

        List<Location> Locations = new ArrayList<>();
        Locations.add(one);
        Locations.add(two);
        Locations.add(three);
        Locations.add(four);
        Locations.add(five);
        Locations.add(six);
        Locations.add(seven);

        GeneticAlgorithm2 ga = new GeneticAlgorithm2(1000, 0.75, 0.2, 3, Locations.size() * Locations.size(), 3, 42, Locations);
        Individual bestIndividual = ga.mainLoop();
        System.out.println("done");
    }
}
