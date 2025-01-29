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

    Route(List<Location> route) throws IOException {
        this.finalRoute = route;

        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "FeatureCollection");

        JSONArray features = new JSONArray();

        for(int i = 0; i < this.finalRoute.size(); i++)
        {

            double[] coord = new double[2];
            coord[0] = this.finalRoute.get(i).getLat();
            coord[1] = this.finalRoute.get(i).getLon();

            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");

            JSONObject geometry = new JSONObject();
            geometry.put("type", "Point");
            geometry.put("coordinates", new JSONArray(coord));

            feature.put("geometry", geometry);
            features.put(feature);
        }

        geoJson.put("features", features);

        try
        {
            File file = new File("src/main/java/org/example/output.txt");
            FileWriter fw = new FileWriter(file);
            fw.write(geoJson.toString(4));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

}
