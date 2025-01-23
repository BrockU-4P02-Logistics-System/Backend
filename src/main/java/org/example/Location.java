package org.example;

public class Location {

    private double lat;
    private double lon;
    int id;

    Location(double lat, double lon, int id)
    {
        if(lat < -90 || lat > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }

        if(lon < -90 || lon > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        this.lat = lat;
        this.lon = lon;
        this.id = id;
    }

    public double getLat()
    {
        return this.lat;
    }

    public double getLon()
    {
        return this.lon;
    }

    public double getID()
    {
        return this.id;
    }

    @Override
    public String toString()
    {
        return "Location: " + id;
    }
}
