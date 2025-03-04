package org.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClusteringResultWrapper {
    ConcurrentHashMap<String, Long> cache;
    Map<Integer, Integer> clusterAssignments;
    public ClusteringResultWrapper(Map<Integer, Integer> clusterAssignments, ConcurrentHashMap<String, Long> distanceCache) {
        this.clusterAssignments = clusterAssignments;
        this.cache = distanceCache;
    }

    public Map<Integer, Integer> getClusterAssignments() {
        return clusterAssignments;
    }

    public ConcurrentHashMap<String, Long> getDistanceCache() {
        return this.cache;
    }
}
