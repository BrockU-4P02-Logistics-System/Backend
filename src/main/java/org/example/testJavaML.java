package org.example;

import com.graphhopper.GraphHopper;
import net.sf.javaml.clustering.KMedoids;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.DenseInstance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class testJavaML {
    public ClusteringResultWrapper cluster(int k, int iterations, List<Location> locations, GraphHopper hopper) {

        // Convert data to Java-ML dataset (each instance represents an index)
        Dataset dataset = new DefaultDataset();
        for (Location l : locations) {
            double[] values = new double[1];
            values[0] = l.getID();
            Instance instance = new DenseInstance(values, l.getID());
            dataset.add(instance);
        }

        // Apply K-Medoids clustering using the custom distance function
        PrecomputedDistance distances = new PrecomputedDistance(locations, hopper);
        KMedoids kMedoids = new KMedoids(k, iterations, distances);
        Dataset[] clusters = kMedoids.cluster(dataset);

        // Store cluster assignments (Location ID -> Cluster ID)
        Map<Integer, Integer> clusterAssignments = new ConcurrentHashMap<>();

        System.out.println("K-Medoids Clustering Results:");
        for (int clusterId = 0; clusterId < clusters.length; clusterId++) {
            System.out.print("Cluster " + (clusterId + 1) + ": ");
            for (Instance instance : clusters[clusterId]) {
                int locationID = (int) instance.value(0);
                clusterAssignments.put(locationID, clusterId); // Assign each location to a cluster
                System.out.print(locationID + " ");
            }
            System.out.println();
        }

        for (Location l : locations){
            l.setCluster(clusterAssignments.get(l.getID()));
        }

        return new ClusteringResultWrapper(locations, distances.cache);
    }
}
