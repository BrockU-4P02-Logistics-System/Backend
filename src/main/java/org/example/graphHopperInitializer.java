package org.example;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.util.CustomModel;
import com.graphhopper.json.Statement;

import java.util.ArrayList;
import java.util.List;

public class graphHopperInitializer {
    private final List<GraphHopper> hoppers;
    private final List<String> profileNames;
    public boolean[] options;

    // Constructor for initializing the GraphHopper instances for all profiles
    public graphHopperInitializer() {
        this.hoppers = new ArrayList<>();
        profileNames = new ArrayList<>();

        // Generate all 32 permutations of user options
        for (int i = 0; i < 32; i++) {
            boolean[] options = intToBooleanArray(i, 5);
            String profileName = generateProfileName(options);
            profileNames.add(profileName);

            // Store in unique cache folder for this profile
            String profileDir = "graph-cache-" + profileName;

            // Create a new GraphHopper instance for each profile
            GraphHopper hopper = new GraphHopper();
            hopper.setOSMFile("ontario-latest.osm.pbf");
            hopper.setGraphHopperLocation(profileDir);
            hopper.setEncodedValuesString("road_class,road_environment,toll");
            // Create `CustomModel` for this permutation
            CustomModel customModel = getCustomModel(options);

            // Create `CustomProfile`
            Profile profile = new CustomProfile(profileName)
                    .setVehicle("car")
                    .setWeighting("custom")
                    .putHint("custom_model", customModel);

            // Set profile for this instance
            hopper.setProfiles(new Profile[]{profile});
            hopper.getCHPreparationHandler().setCHProfiles(new CHProfile(profileName));
            // Import the graph for this specific profile
            hopper.importOrLoad();

            // Add the GraphHopper instance to the list
        }
    }

    // Constructor to load from a specific graph directory with user options
    public graphHopperInitializer(boolean[] options) {
        this.hoppers = new ArrayList<>();
        this.options = options;
        profileNames = new ArrayList<>();
        // Generate the profile name based on the provided options
        String profileName = generateProfileName(options);
        profileNames.add(profileName);

        // Store in unique cache folder for this profile
        String profileDir = "graph-cache-" + profileName;

        // Create a new GraphHopper instance for this profile
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile("ontario-latest.osm.pbf");
        hopper.setGraphHopperLocation(profileDir);

        // Create `CustomModel` for this permutation
        CustomModel customModel = getCustomModel(options);

        // Create `CustomProfile`
        Profile profile = new CustomProfile(profileName)
                .setVehicle("car")
                .setWeighting("custom")
                .putHint("custom_model", customModel);

        // Set profile for this instance
        hopper.setProfiles(new Profile[]{profile});
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile(profileName));
        // Import the graph for this specific profile
        hopper.importOrLoad();

        // Add the GraphHopper instance to the list
        hoppers.add(hopper);
    }

    // 🔥 Abstracted method to generate `CustomModel`
    public static CustomModel getCustomModel(boolean[] options) {
        CustomModel customModel = new CustomModel();

        if (options[0]) { // Avoid Highways
            customModel.addToPriority(Statement.If("road_class == MOTORWAY || road_class == TRUNK || road_class == PRIMARY", Statement.Op.MULTIPLY, "0.01"));
        }
        if (options[1]) { // Avoid Tolls
            customModel.addToPriority(Statement.If("toll == ALL || toll == HGV", Statement.Op.MULTIPLY, "0.01"));
        }
        if (options[2]) { // Avoid Unpaved Roads
            customModel.addToPriority(Statement.If("road_class == TRACK", Statement.Op.MULTIPLY, "0.01"));
        }
        if (options[3]) { // Avoid Ferries
            customModel.addToPriority(Statement.If("road_environment == FERRY", Statement.Op.MULTIPLY, "0.01"));
        }
        if (options[4]) { // Avoid Tunnels
            customModel.addToPriority(Statement.If("road_environment == TUNNEL", Statement.Op.MULTIPLY, "0.01"));
        }

        return customModel;
    }

    // 🔥 Converts integer to boolean array for all 32 permutations
    private static boolean[] intToBooleanArray(int num, int size) {
        boolean[] result = new boolean[size];
        for (int i = 0; i < size; i++) {
            result[i] = (num & (1 << i)) != 0;
        }
        return result;
    }

    // 🔥 Generate unique profile name based on user settings
    static String generateProfileName(boolean[] options) {
        return "custom_car_" +
                (options[0] ? "t" : "f") +  // Highways
                (options[1] ? "t" : "f") +  // Tolls
                (options[2] ? "t" : "f") +  // Unpaved
                (options[3] ? "t" : "f") +  // Ferries
                (options[4] ? "t" : "f");   // Tunnels
    }

    public GraphHopper getHopper() {
        return hoppers.get(0);
    }
}
