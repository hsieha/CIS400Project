package com.example.livelyturtle.androidar;

import java.util.ArrayList;
import java.lang.Math;
import android.location.Location;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;
import static android.location.Location.distanceBetween;
import java.awt.*;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class Street extends WorldObject {
    public Street(String name, Location a, Location b) {
        this.name = name;
        this.coordinates = new ArrayList<Location>();
        this.coordinates.add(a);
        this.coordinates.add(b);
    }

    public void setCoordinates(Location a, Location b) {
        this.coordinates = new ArrayList<Location>();
        this.coordinates.add(a);
        this.coordinates.add(b);
    }

    /// TODO: Darren, you can use this base to write your math code

    //takes in azimuth and moverio gps location

    //ASSUMPTION: arcs are so close, they are effectively lines, so use line intersection math
    //GOING TO ASSUME THAT STREETS STORES 2 Location Objects
    //Convert everything into a grid
    public boolean doesIntersect(float azimuth, Location my_location) {

        double my_lat = my_location.getLatitude();
        double my_long = my_location.getLongitude();

        double[] my_p1 = new double[]{0.0, 0.0};

        double[] my_p2 = new double[]{500 * Math.cos(azimuth), Math.sin(500 * azimuth)};

        ArrayList<Location> street_coordinates = this.getCoordinates();
        Location street_loc1 = street_coordinates.get(0);
        Location street_loc2 = street_coordinates.get(1);

        float[] results = new float[3];

        Location.distanceBetween(my_lat, my_long, street_loc1.getLatitude(), street_loc1.getLongitude(), results);
        float dist_to_loc1 = results[0];
        float loc1_angle = results[1];

        double[] street_p1 = new double[]{dist_to_loc1 * Math.cos((double) loc1_angle), dist_to_loc1 * Math.sin((double) loc1_angle)};

        Location.distanceBetween(my_lat, my_long, street_loc2.getLatitude(), street_loc2.getLongitude(), results);
        float dist_to_loc2 = results[0];
        float loc2_angle = results[1];

        double[] street_p2 = new double[]{dist_to_loc1 * Math.cos((double) loc2_angle), dist_to_loc1 * Math.sin((double) loc2_angle)};


        double street_slope = 0;
        double street_b = 0;
        double my_slope = 0;
        double my_b = 0;

        //see if both the lines are vertical
        if (my_p1[0] == my_p2[0] && street_p1[0] == street_p2[0]){
            //are their x value the same?
            if(my_p1[0] == street_p1[0]) {
                //if so check if they overlap at some point
                if(street_p1[1] < my_p2[1] || street_p2[1] < my_p2[1]){
                    return true;
                } else {
                    return false;
                }
            }
        }
        //if you are vertical (due east or west)
        else if(my_p1[0] == my_p2[0]){
            street_slope = (street_p2[1] - street_p1[1])/(street_p2[0] - street_p1[0]);
            street_b = street_p1[1] - street_slope*my_p1[0];


            double y = street_slope*my_p1[0] + street_b;

            if(Double.compare(y, Math.min(my_p1[1], my_p2[1])) >= 0 && Double.compare(y, Math.max(my_p1[1], my_p2[1])) <= 0 &&
                    Double.compare(y, Math.min(street_p1[1], street_p2[1])) >= 0 && Double.compare(y, Math.max(street_p1[1], street_p2[1])) <= 0){
                return true;
            } else {
                return false;
            }
        }
        //if street is vertical
        else if (street_p1[0] == street_p2[0]){
            my_slope = (my_p2[1] - my_p1[0]) / (my_p2[0] - my_p1[0]);
            my_b = my_p1[1] - my_slope*my_p1[0];


            double y = my_slope*street_p1[0] + my_b;

            if(Double.compare(y, Math.min(my_p1[1], my_p2[1])) >= 0 && Double.compare(y, Math.max(my_p1[1], my_p2[1])) <= 0 &&
                    Double.compare(y, Math.min(street_p1[1], street_p2[1])) >= 0 && Double.compare(y, Math.max(street_p1[1], street_p2[1])) <= 0){
                return true;
            } else {
                return false;
            }

        }
        //if neither of them are vertical
        else{
            street_slope = (street_p2[1] - street_p1[1])/(street_p2[0] - street_p1[0]);
            street_b = street_p1[1] - street_slope*my_p1[0];

            my_slope = (my_p2[1] - my_p1[0]) / (my_p2[0] - my_p1[0]);
            my_b = my_p1[1] - my_slope*my_p1[0];

            //if slopes are same, lines are parellel
            if(street_slope == my_slope){
                //check if have same intercept b
                if(street_b == my_b){
                    //if so, check if there is overlap
                    if(street_p1[0] < my_p2[0] || street_p2[0] < my_p2[0]){
                        //if overlap, return true, otherwise return false
                        return true;
                    } else {
                        return false;
                    }
                }
                //else parallel lines that never touch
                return false;
            }

            //otherwise not parallel
            double x = -(my_b - street_b) / (my_slope - street_slope);

            if(Double.compare(x, Math.min(my_p1[0], my_p2[0])) >= 0 && Double.compare(x, Math.max(my_p1[0], my_p2[0])) <= 0 &&
                    Double.compare(x, Math.min(street_p1[0], street_p2[0])) >= 0 && Double.compare(x, Math.max(street_p1[0], street_p2[0])) <= 0){
                return true;
            } else {
                return false;
            }

        }

        //e.g. example:
        // 39.951793, -75.201080 (harrison college house)
        // 39.952357, -75.200123 (harnwell college house)
        // distance of roughly 320 or so meters
        return false;
    }
}
