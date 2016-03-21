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
    public static final Coordinate COMPASS = new Coordinate(COMPASS_LAT, COMPASS_LONG);


    //NOTE from Darren: if the lat/long gets too large
    // errors will become larger further away from origin
    //if it happens, need to use something like mercator projection to fix
    public Coordinate (double la, double lo) {
        latitude = la;
        longitude = lo;

        float[] results = new float[3];
        Location.distanceBetween(COMPASS_LAT, COMPASS_LONG, la, lo, results);

//        System.out.println("***COORD START DEBUG OUT");
//        System.out.println(results[0]);
//        System.out.println(results[1]);
//        System.out.println(results[2]);

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
        //System.out.println("***COORD END DEBUG OUT");


        float dist_to_coor = results[0];
        float angle_to_coor = results[1];

        z = -dist_to_coor * Math.cos((double) angle_to_coor * Math.PI / 180.);
        x = dist_to_coor * Math.sin((double) angle_to_coor * Math.PI / 180.);

    }

    public double dist(Coordinate c) {
        return Math.sqrt(Math.pow(this.latitude-c.latitude,2)+Math.pow(this.longitude-c.longitude,2));
    }

    public static double cross(Coordinate p1, Coordinate p2) {
        return p1.latitude * p2.longitude - p1.longitude * p2.latitude;
    }

    public static Coordinate subtract(Coordinate p2, Coordinate p1) {
        return new Coordinate(p2.latitude-p1.latitude,p2.longitude-p1.longitude);
    }

    public static Coordinate add(Coordinate p1, Coordinate p2) {
        return new Coordinate(p2.latitude+p1.latitude,p2.longitude+p1.longitude);
    }

    public static Coordinate mult(Coordinate p, double s) {
        return new Coordinate(p.latitude*s, p.longitude*s);
    }

    public static double dot(Coordinate p1, Coordinate p2) {
        return p1.latitude * p2.latitude + p1.longitude * p2.longitude;
    }

    public static boolean closeTo(double x, double y) {
        if (Math.abs(x - y) < 0.00001) {
            return true;
        }
        return false;
    }

    // get a coordinate by specifying xz values
    private Coordinate() {}
    public static Coordinate fromXZ(double x, double z) {
        Coordinate c = new Coordinate();
        c.x = x;
        c.z = z;

        // gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
        double latAdj = -(z/111111.);
        double longAdj = x/(111111.*Math.cos(COMPASS_LAT*Math.PI/180.));
        c.latitude = COMPASS_LAT + latAdj;
        c.longitude = COMPASS_LONG + longAdj;

        return c;
    }

    @Override
    public String toString() {
        return "(" + latitude + "," + longitude + ")";

    }

}
