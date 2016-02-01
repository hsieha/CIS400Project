package com.example.livelyturtle.androidar;

import android.location.Location;

import static android.location.Location.distanceBetween;

/**
 * Created by LivelyTurtle on 1/30/2016.
 */

public class Coordinate {
    public double latitude;
    public double longitude;
    public double x;
    public double y;

    //compass coordinates: 39.952258, -75.197008
    private double compass_lat = 39.952258;
    private double compass_long = -75.197008;

    //NOTE from Darren: if the lat/long gets too large
    // errors will become larger further away from origin
    //if it happens, need to use something like mercator projection to fix
    public Coordinate (double la, double lo) {
        latitude = la;
        longitude = lo;

        float[] results = new float[3];
        Location.distanceBetween(compass_lat, compass_long, la, lo, results);

        float dist_to_coor = results[0];
        float angle_to_coor = results[1];

        x = dist_to_coor * Math.cos((double) angle_to_coor);
        y = dist_to_coor * Math.sin((double) angle_to_coor);

    }
}
