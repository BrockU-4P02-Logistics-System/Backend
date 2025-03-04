package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Route {

    List<Location> finalRoute;

    Route(List<Location> route, String filePath) throws IOException {
        this.finalRoute = route;

        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "FeatureCollection");

        JSONArray features = new JSONArray();

        for (Location location : this.finalRoute) {

            double[] coord = new double[2];
            coord[0] = location.getLat();
            coord[1] = location.getLon();

            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");

            JSONObject geometry = new JSONObject();
            geometry.put("type", "Point");
            geometry.put("ID", location.id);
            geometry.put("Driver-ID",location.clusterid);
            geometry.put("coordinates", new JSONArray(coord));

            feature.put("geometry", geometry);
            features.put(feature);
        }

        geoJson.put("features", features);

        try
        {
            File file = new File(filePath);
            FileWriter fw = new FileWriter(file);
            fw.write(geoJson.toString(4));
            fw.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public List<Location> getRoute(){
        return this.finalRoute;
    }
}
