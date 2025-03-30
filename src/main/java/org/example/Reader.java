package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Reader {

    public List<Location> locations;
    public int numberDrivers;

    public boolean returnToStart;
    public boolean avoidHighways;
    public boolean avoidTolls;
    public boolean avoidUnpavedRoads;
    public boolean avoidFerries;
    public boolean avoidTracks;

    public boolean[] flags;

    public Reader(String filePath) throws IOException
    {
        try
        {
            this.locations = new ArrayList<>();
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            this.flags = new boolean[3]; // Avoid highways, avoid tolls, avoid unpaved roads, avoid ferries, avoid tracks

            JSONObject geoJson = new JSONObject(content);

            this.numberDrivers = geoJson.optInt("total_drivers", 1); // Default to 1 if missing
            this.returnToStart = geoJson.optBoolean("return_to_start", true); // Default true

            // These are the flags being set in graph hopper
            this.avoidHighways = geoJson.optBoolean("avoid_highways", false); // Default false
            this.avoidTolls = geoJson.optBoolean("avoid_tolls", false); // Default false
            this.avoidFerries = geoJson.optBoolean("avoid_ferries", false); // Default false

            this.flags[0] = this.avoidHighways;
            this.flags[1] = this.avoidTolls;
            this.flags[2] = this.avoidFerries;

            JSONArray features = geoJson.getJSONArray("features");

            for(int i = 0; i < features.length(); i++)
            {
                JSONObject feature = features.getJSONObject(i);

                JSONObject properties = feature.getJSONObject("properties");
                JSONObject geometry = feature.getJSONObject("geometry");
                String type = geometry.getString("type");
                int id = properties.getInt("id");

                if ("Point".equalsIgnoreCase(type))
                {
                    JSONArray coordinates = geometry.getJSONArray("coordinates");

                    // GeoJSON standard: coordinates are in [longitude, latitude] order
                    double lon = coordinates.getDouble(0);
                    double lat = coordinates.getDouble(1);

                    // Create a new Location object with the parsed lat, lon, and id
                    Location location = new Location(lat, lon, id);
                    this.locations.add(location);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
