package com.example.livelyturtle.androidar;

import android.location.Location;

/**
 * Created by LivelyTurtle on 1/30/2016.
 */

public class Coordinate {
    public double latitude;
    public double longitude;
    public double x; //longitude equivalent in opengl
    public double z; //latitude equivalent in opengl

    // compass coordinates: 39.952258, -75.197008
    public static final double COMPASS_LAT = 39.952258;
    public static final double COMPASS_LONG = -75.197008;

    //NOTE from Darren: if the lat/long gets too large
    // errors will become larger further away from origin
    //if it happens, need to use something like mercator projection to fix
    public Coordinate (double la, double lo) {
        latitude = la;
        longitude = lo;

        float[] results = new float[3];
        Location.distanceBetween(COMPASS_LAT, COMPASS_LONG, la, lo, results);

        //System.out.println(results[0]);

        //System.out.println("trying out distanceTo");
        Location compass = new Location("compass");
        compass.setLatitude(COMPASS_LAT);
        compass.setLongitude(COMPASS_LONG);
        Location point = new Location("point");
        point.setLatitude(la);
        //System.out.println("object la: " + la);
        point.setLongitude(lo);
        //System.out.println("object lo: " + lo);

        float distanceto = compass.distanceTo(point);
        //System.out.println("distance from compass: " + distanceto);



        float dist_to_coor = results[0];
        float angle_to_coor = results[1];

        z = dist_to_coor * Math.cos((double) angle_to_coor);
        x = dist_to_coor * Math.sin((double) angle_to_coor);

    }
}
