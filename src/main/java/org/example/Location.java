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

        if(lon < -180 || lon > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180.");
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

    public int getID()
    {
        return this.id;
    }

    @Override
    public String toString()
    {
        return "Location: " + id;
    }
}
