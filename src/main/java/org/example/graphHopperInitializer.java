package org.example;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.util.CustomModel;
import com.graphhopper.json.Statement;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;

import java.util.ArrayList;
import java.util.List;

public class graphHopperInitializer {
    // ----------------------------------------------------
    // 1) A single static GraphHopper to avoid re-importing
    // ----------------------------------------------------
    private static GraphHopper hopper = null;

    // Keep track of all 32 profile names in one place
    private static final List<String> ALL_PROFILE_NAMES = new ArrayList<>();

    // ----------------------------------------------------
    // 2) Per-instance fields
    // ----------------------------------------------------
    final boolean[] options;
    private final String selectedProfileName;

    // ----------------------------------------------------
    // 3) Default constructor: builds all 32 profiles
    //    (only if hopper == null)
    // ----------------------------------------------------
    public graphHopperInitializer() {
        if (hopper == null) {
            // Create a single GraphHopper instance
            hopper = new GraphHopper();
            hopper.setOSMFile("north-america-latest.osm.pbf");
            // All data will be saved into 'graph-cache/'
            hopper.setGraphHopperLocation("graph-cache");
            hopper.setEncodedValuesString("road_class,road_environment,toll");

            // Prepare lists for storing Profiles + CHProfiles
            List<Profile> profiles = new ArrayList<>();
            List<CHProfile> chProfiles = new ArrayList<>();

            // Generate all 8 permutations
            for (int i = 0; i < 8; i++) {
                boolean[] opts = intToBooleanArray(i, 3);
                String profileName = generateProfileName(opts);
                ALL_PROFILE_NAMES.add(profileName);

                // Build the custom model
                CustomModel customModel = getCustomModel(opts);

                // Create the Profile
                Profile customProfile = new CustomProfile(profileName)
                        .setVehicle("car")
                        .setWeighting("custom")
                        .putHint("custom_model", customModel);

                // Collect them
                profiles.add(customProfile);
                chProfiles.add(new CHProfile(profileName));
            }

            // Register all profiles in a single GraphHopper instance
            hopper.setProfiles(profiles);
            hopper.getCHPreparationHandler().setCHProfiles(chProfiles);

            // Import/Load the graph data once for all 32 permutations
            hopper.importOrLoad();
        }

        // If no boolean[] given, default to [false,false,false,false,false]
        // or do whatever you want as a "no options" default
        this.options = new boolean[] { false, false, false };
        this.selectedProfileName = generateProfileName(this.options);
    }

    // ----------------------------------------------------
    // 4) Overloaded constructor: DOES NOT rebuild the graph
    //    Just picks your profile from the existing 32
    // ----------------------------------------------------
    public graphHopperInitializer(boolean[] options) {
        // Make sure we don't re-import if it's already loaded
        if (hopper == null) {
            throw new IllegalStateException(
                    "GraphHopper has not been initialized yet. " +
                            "Call the no-arg constructor first or build a static block."
            );
        }
        // Store these as "my selected profile"
        this.options = options;
        this.selectedProfileName = generateProfileName(options);

        // Optional: you could check if the name actually exists in ALL_PROFILE_NAMES
        // to catch mistakes:
        if (!ALL_PROFILE_NAMES.contains(this.selectedProfileName)) {
            throw new IllegalArgumentException("Unknown profile name: " + this.selectedProfileName);
        }
    }

    // Optionally just expose the single GraphHopper itself
    public GraphHopper getHopper() {
        return hopper;
    }

    // ----------------------------------------------------
    // 5) The usual helper methods
    // ----------------------------------------------------
    public static CustomModel getCustomModel(boolean[] options) {
        CustomModel customModel = new CustomModel();

        if (options[0]) { // Avoid Highways
            customModel.addToPriority(Statement.If(
                    "road_class == MOTORWAY || road_class == TRUNK || road_class == PRIMARY",
                    Statement.Op.MULTIPLY, "0.01"));
        }
        if (options[1]) { // Avoid Tolls
            customModel.addToPriority(Statement.If(
                    "toll == ALL || toll == HGV",
                    Statement.Op.MULTIPLY, "0.01"));
        }
        if (options[2]) { // Avoid Unpaved Roads
            customModel.addToPriority(Statement.If(
                    "road_environment == FERRY",
                    Statement.Op.MULTIPLY, "0.01"));
        }
        return customModel;
    }

    private static boolean[] intToBooleanArray(int num, int size) {
        boolean[] result = new boolean[size];
        for (int i = 0; i < size; i++) {
            result[i] = (num & (1 << i)) != 0;
        }
        return result;
    }

    static String generateProfileName(boolean[] options) {
        return "custom_car_" +
                (options[0] ? "t" : "f") +
                (options[1] ? "t" : "f") +
                (options[2] ? "t" : "f");
    }
}
