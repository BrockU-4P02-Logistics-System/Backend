package org.example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClusteringResultWrapper {
    ConcurrentHashMap<String, Long> cache;
    List<Location> locations;
    public ClusteringResultWrapper(List<Location> locations, ConcurrentHashMap<String, Long> distanceCache) {
        this.locations = locations;
        this.cache = distanceCache;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public ConcurrentHashMap<String, Long> getDistanceCache() {
        return this.cache;
    }
}
