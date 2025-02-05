package org.example;

import java.util.ArrayList;
import java.util.List;

public class GAHarness {
    public static void main(String[] args) {

        List<Location> locations = new ArrayList<>();

        locations.add(new Location(43.642567, -79.387054, 1)); // CN Tower
        locations.add(new Location(43.667710, -79.394777, 2)); // Royal Ontario Museum
        locations.add(new Location(43.653908, -79.384293, 3)); // Toronto City Hall
        locations.add(new Location(43.662892, -79.395656, 4)); // University of Toronto
        locations.add(new Location(43.646548, -79.463706, 5)); // High Park
        locations.add(new Location(43.677717, -79.624819, 6)); // Toronto Pearson Airport
        locations.add(new Location(43.650313, -79.359533, 7)); // Distillery District
        locations.add(new Location(43.649093, -79.371713, 8)); // St. Lawrence Market
        locations.add(new Location(43.642403, -79.386345, 9)); // Ripley's Aquarium
        locations.add(new Location(43.653606, -79.392512, 10)); // Art Gallery of Ontario
        locations.add(new Location(43.620417, -79.378134, 11)); // Toronto Islands
        locations.add(new Location(43.678002, -79.409445, 12)); // Casa Loma
        locations.add(new Location(43.652620, -79.381936, 13)); // Nathan Phillips Square
        locations.add(new Location(43.656087, -79.380188, 14)); // Yonge-Dundas Square
        locations.add(new Location(43.641438, -79.389353, 15)); // Rogers Centre
        locations.add(new Location(43.643466, -79.379099, 16)); // Scotiabank Arena
        locations.add(new Location(43.716100, -79.338880, 17)); // Ontario Science Centre
        locations.add(new Location(43.817699, -79.185929, 18)); // Toronto Zoo
        locations.add(new Location(43.654438, -79.380699, 19)); // Eaton Centre
        locations.add(new Location(43.654568, -79.402356, 20)); // Kensington Market
        locations.add(new Location(43.638731, -79.381349, 21)); // Harbourfront Centre
        locations.add(new Location(43.639250, -79.406393, 22)); // Fort York
        locations.add(new Location(43.684321, -79.365845, 23)); // Evergreen Brick Works
        locations.add(new Location(43.668180, -79.296473, 24)); // The Beaches
        locations.add(new Location(43.727539, -79.363619, 25)); // Toronto Botanical Garden
        locations.add(new Location(43.725749, -79.338713, 26)); // Aga Khan Museum
        locations.add(new Location(43.646912, -79.377638, 27)); // Hockey Hall of Fame
        locations.add(new Location(43.633339, -79.418869, 28)); // BMO Field
        locations.add(new Location(43.667135, -79.301529, 29)); // Woodbine Beach
        locations.add(new Location(43.664473, -79.362571, 30)); // Riverdale Farm
        locations.add(new Location(43.661667, -79.372500, 31)); // Allan Gardens
        locations.add(new Location(43.638200, -79.385500, 32)); // Toronto Music Garden
        locations.add(new Location(43.644100, -79.368900, 33)); // Sugar Beach
        locations.add(new Location(43.647778, -79.413611, 34)); // Trinity Bellwoods Park
        locations.add(new Location(43.662500, -79.391111, 35)); // Queen's Park
        locations.add(new Location(43.668889, -79.394722, 36)); // Gardiner Museum
        locations.add(new Location(43.667778, -79.400000, 37)); // Bata Shoe Museum
        locations.add(new Location(43.678333, -79.405833, 38)); // Spadina Museum
        locations.add(new Location(43.773611, -79.507778, 39)); // Black Creek Pioneer Village
        locations.add(new Location(43.628889, -79.414722, 40)); // Ontario Place
        locations.add(new Location(43.640000, -79.380000, 41)); // Toronto Harbour
        locations.add(new Location(43.636111, -79.455000, 42)); // Sunnyside Pavilion
        locations.add(new Location(43.620833, -79.478611, 43)); // Humber Bay Park
        locations.add(new Location(43.727500, -79.363611, 44)); // Edwards Gardens
        locations.add(new Location(43.805000, -79.130000, 45)); // Rouge National Urban Park
        locations.add(new Location(43.743056, -79.196111, 46)); // Guild Park and Gardens
        locations.add(new Location(43.705000, -79.235000, 47)); // Scarborough Bluffs
        locations.add(new Location(43.676389, -79.287500, 48)); // R.C. Harris Water Treatment Plant


        GeneticAlgorithm2 ga = new GeneticAlgorithm2(1000, 0.75, 0.2, 3, locations.size() * locations.size(), 3, 42, locations);
        Individual bestIndividual = ga.mainLoop();
    }
}
