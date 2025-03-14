package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Route {

    List<Location> finalRoute;

    Route(List<Location> route, String filePath) throws IOException
    {
        this.finalRoute = route;

        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "FeatureCollection");

        JSONArray features = new JSONArray();

        for (Location location : this.finalRoute)
        {
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");

            // Correcting coordinate order (GeoJSON: [lon, lat])
            JSONArray coordinates = new JSONArray();
            coordinates.put(location.getLon()); // Longitude first
            coordinates.put(location.getLat()); // Latitude second

            JSONObject geometry = new JSONObject();
            geometry.put("type", "Point");
            geometry.put("coordinates", coordinates);

            // Moving metadata to properties
            JSONObject properties = new JSONObject();
            properties.put("Driver-ID", location.clusterid);
            properties.put("ID", location.id);

            feature.put("geometry", geometry);
            feature.put("properties", properties);
            features.put(feature);
        }

        geoJson.put("features", features);

        // Improved file writing (auto-closes FileWriter)
        try (FileWriter fw = new FileWriter(new File(filePath)))
        {
            fw.write(geoJson.toString(4)); // Pretty-print with indentation
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public List<Location> getRoute() {
        return this.finalRoute;
    }
}
