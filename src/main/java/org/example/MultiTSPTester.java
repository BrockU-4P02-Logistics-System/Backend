package org.example;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.util.ArrayList;
import java.util.List;

public class MultiTSPTester {
    private final GraphHopper hopper;
    private final List<Location> locations;
    private final int numVehicles;

    public MultiTSPTester(int numVehicles, List<Location> locations) {
        this.numVehicles = numVehicles;
        this.locations = locations;

        // Initialize GraphHopper
        hopper = new GraphHopper();
        hopper.setOSMFile("ontario.osm.pbf");
        hopper.setGraphHopperLocation("graph-cache");
        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest"));
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));
        hopper.importOrLoad();
    }

    public void solveRoutingProblem(int timeLimit) {
        VehicleRouter router = new VehicleRouter(locations, hopper, numVehicles);
        //router.solveTSP(timeLimit);
    }

    public static void main(String[] args) {
        List<Location> locations = new ArrayList<>();

        //Toronto
        locations.add(new Location(43.662500, -79.391111, 1)); // Queen's Park

        // St. Catharines
        locations.add(new Location(43.159374, -79.246862, 2)); // 1st Street Louth
        locations.add(new Location(43.158912, -79.245678, 3)); // 2nd Avenue
        locations.add(new Location(43.160123, -79.248456, 4)); // 3rd Avenue Louth
        locations.add(new Location(43.161234, -79.249567, 5)); // 3rd Street
        locations.add(new Location(43.162345, -79.250678, 6)); // 3rd Street Louth
        locations.add(new Location(43.163456, -79.251789, 7)); // 4th Avenue
        locations.add(new Location(43.164567, -79.252890, 8)); // 5th Avenue Louth
        locations.add(new Location(43.165678, -79.253901, 9)); // 5th Street Louth
        locations.add(new Location(43.166789, -79.254012, 10)); // 7 Oaks Circle
        locations.add(new Location(43.167890, -79.255123, 11)); // 7th Street Louth
        locations.add(new Location(43.168901, -79.256234, 12)); // 8th Avenue Louth
        locations.add(new Location(43.169012, -79.257345, 13)); // Abbey Avenue
        locations.add(new Location(43.170123, -79.258456, 14)); // Abbot Street West
        locations.add(new Location(43.171234, -79.259567, 15)); // Abbott Street
        locations.add(new Location(43.172345, -79.260678, 16)); // Aberdeen Circle
        locations.add(new Location(43.173456, -79.261789, 17)); // Abraham Drive
        locations.add(new Location(43.174567, -79.262890, 18)); // Academy Street
        locations.add(new Location(43.175678, -79.263901, 19)); // Acadia Crescent
        locations.add(new Location(43.176789, -79.264012, 20)); // Adams Street
        locations.add(new Location(43.177890, -79.265123, 21)); // Addison Drive

        // Barrie
        locations.add(new Location(44.389123, -79.690456, 22)); // 10th Line
        locations.add(new Location(44.388234, -79.689567, 23)); // Aconley Court
        locations.add(new Location(44.387345, -79.688678, 24)); // Adam Avenue
        locations.add(new Location(44.386456, -79.687789, 25)); // Adam Drive
        locations.add(new Location(44.385567, -79.686890, 26)); // Adam Street
        locations.add(new Location(44.384678, -79.685901, 27)); // Addison Trail
        locations.add(new Location(44.383789, -79.684012, 28)); // Adelaide Street
        locations.add(new Location(44.382890, -79.683123, 29)); // Agnes Street
        locations.add(new Location(44.381901, -79.682234, 30)); // Aikens Crescent
        locations.add(new Location(44.380912, -79.681345, 31)); // Albert Street
        locations.add(new Location(44.379923, -79.680456, 32)); // Aldergrove Close
        locations.add(new Location(44.378934, -79.679567, 33)); // Aleda Street
        locations.add(new Location(44.377945, -79.678678, 34)); // Alexander Avenue
        locations.add(new Location(44.376956, -79.677789, 35)); // Alfred Street
        locations.add(new Location(44.375967, -79.676890, 36)); // Algonquin Trail
        locations.add(new Location(44.374978, -79.675901, 37)); // Alliance Boulevard
        locations.add(new Location(44.373989, -79.674012, 38)); // Allsop Crescent
        locations.add(new Location(44.372990, -79.673123, 39)); // Alva Street
        locations.add(new Location(44.371901, -79.672234, 40)); // Ambler Bay
        locations.add(new Location(44.370912, -79.671345, 41)); // Amelia Street

        MultiTSPTester tester = new MultiTSPTester(2, locations);
        long startTime = System.currentTimeMillis();
        tester.solveRoutingProblem(20);
        System.out.println(System.currentTimeMillis() - startTime + " ms");
    }
}
