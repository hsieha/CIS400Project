package com.example.livelyturtle.androidar;

import java.util.ArrayList;
import java.lang.Math;
import android.location.Location;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class Street extends WorldObject {

    private float y = 0.0f;                   //streets height of 0 (might need to adjust)
    private double width = 5;        //width of each street is 5 meters

    public Street(String name, ArrayList<Coordinate> coordinates) {
        super(name, coordinates, 0);
    }

    //returns array_list of the vectors
    public ArrayList<Vector> vectors(){

        ArrayList<Vector> vectors = new ArrayList<Vector>();

        Coordinate p1 = coordinates.get(0);
        Coordinate p2 = coordinates.get(1);

        //slope of street line
        double m = (p2.z - p1.z) / (p2.x - p1.x);
        //slope of perpendicular (negative reciprocal of m)
        double other_m = -(1/m);
        //find the b intercept for both perpendicular lines that intersect p1 and p2
        double p1_other_b = other_m*p1.x - p1.z;
        double p2_other_b = other_m*p2.x - p2.z;

        //need to pick a random point along the line
        double other_x = 0;
        double other_z = other_m*other_x + p1_other_b;
        //then find a unit vector
        double norm_x = (p1.x - other_x);
        double norm_z = (p1.z - other_z);
        double norm_magnitude = Math.sqrt((Math.pow(norm_x, 2) + Math.pow(norm_z,2)));
        double norm_unit_x = norm_x / norm_magnitude;
        double norm_unit_z = norm_z / norm_magnitude;

        boolean right;
        if (other_x > p1.x){
            right = true;
        } else {
            right = false;
        }

        if(right){
            vectors.add(Vector.of((float)(p1.x - width/2.0*norm_unit_x), y, (float)(p1.z - 2.5*norm_unit_z)));
            vectors.add(Vector.of((float)(p1.x + width/2.0*norm_unit_x), y, (float)(p1.z + 2.5*norm_unit_z)));
            vectors.add(Vector.of((float)(p2.x + width/2.0*norm_unit_x), y, (float)(p2.z + 2.5*norm_unit_z)));
            vectors.add(Vector.of((float)(p2.x - width/2.0*norm_unit_x), y, (float)(p2.z - 2.5*norm_unit_z)));
        } else {
            vectors.add(Vector.of((float)(p1.x + width/2.0*norm_unit_x), y, (float)(p1.z + 2.5*norm_unit_z)));
            vectors.add(Vector.of((float)(p1.x - width/2.0*norm_unit_x), y, (float)(p1.z - 2.5*norm_unit_z)));
            vectors.add(Vector.of((float)(p2.x - width/2.0*norm_unit_x), y, (float)(p2.z - 2.5*norm_unit_z)));
            vectors.add(Vector.of((float)(p2.x + width/2.0*norm_unit_x), y, (float)(p2.z + 2.5*norm_unit_z)));
        }

        return vectors;
    }

    //order of vertices
    public ArrayList<Short> vector_order() {
        ArrayList<Short> order = new ArrayList<Short>();   //only 4 vertices, index order is easy to make
        order.add((short) 0);
        order.add((short) 1);
        order.add((short) 2);
        order.add((short) 0);
        order.add((short) 2);
        order.add((short) 3);
        return order;
    }


    ////////////////////////
    //NOT USING BELOW CODE//
    ////////////////////////
    public boolean doesIntersect(float azimuth, Location my_location) {

        double my_lat = my_location.getLatitude();
        double my_long = my_location.getLongitude();

        double[] my_p1 = new double[]{0.0, 0.0};

        double[] my_p2 = new double[]{500 * Math.cos(azimuth), Math.sin(500 * azimuth)};

        ArrayList<Coordinate> street_coordinates = this.getCoordinates();
        Coordinate street_loc1 = street_coordinates.get(0);
        Coordinate street_loc2 = street_coordinates.get(1);

        float[] results = new float[3];

        Location.distanceBetween(my_lat, my_long, street_loc1.latitude, street_loc1.longitude, results);
        float dist_to_loc1 = results[0];
        float loc1_angle = results[1];

        double[] street_p1 = new double[]{dist_to_loc1 * Math.cos((double) loc1_angle), dist_to_loc1 * Math.sin((double) loc1_angle)};

        Location.distanceBetween(my_lat, my_long, street_loc2.latitude, street_loc2.longitude, results);
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
            } else {
                return false;
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
    }
}
