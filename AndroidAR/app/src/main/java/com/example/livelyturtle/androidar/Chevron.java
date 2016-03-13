package com.example.livelyturtle.androidar;

import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;
import java.util.ArrayList;

/**
 * Created by Darren on 2/29/16.
 */
public class Chevron extends WorldObject {

    //width of chevron is less than width of road
    //road width currently set to 5m

    //4 triangle for a chevron

    static final int INDEX_COUNT = 12;
    static final int VERTEX_COUNT = 6;

    Coordinate textCoord;

    //NOTE: Arraylist coordinates only holds 1 coordinate
    //this coordinate is the top center vertx of the chevron
    //must also enter a direction to point the chveron
    //direction given in degrees from north clockwise
    public Chevron(String name, ArrayList<Coordinate> coordinates, float dir){
        super(name, coordinates, 0.2f);

        double lat = 0;
        double lon = 0;
        for (Coordinate coord : this.coordinates) {
            lat += coord.latitude;
            lon += coord.longitude;
        }
        lat -= this.coordinates.get(0).latitude;
        lon -= this.coordinates.get(0).longitude;
    }

    //location of each vertex
    public ArrayList<Moverio3D.Vector> vectors() {

        ArrayList<Moverio3D.Vector> vectors = new ArrayList<Moverio3D.Vector>();

        double x = this.coordinates.get(0).x;
        double z = this.coordinates.get(0).z;

        //given point for top of the chevron
        vectors.add(Vector.of((float)x, (float) height, (float)z));

        //all other points are rotated based on specified direction given for where chevron is point at
        //chevron is being built counterclockwise
        vectors.add(Vector.of((float) (x - 2.0), (float) height, (float) (z + 1)));
        vectors.add(Vector.of((float) (x - 2.0), (float) height, (float) (z + 2)));
        vectors.add(Vector.of((float) x, (float) height, (float) (z + 1)));
        vectors.add(Vector.of((float) (x + 2.0), (float) height, (float) (z + 2)));
        vectors.add(Vector.of((float) (x + 2.0), (float) height, (float) (z + 1)));

        return vectors;
    }

    //order of vertices
    public ArrayList<Short> vector_order(){

        //DARREN: couldn't figure out a smart way to iterate through this
        ArrayList<Short> order = new ArrayList<Short>() {{
            add((short)0);
            add((short)1);
            add((short)3);
            add((short)1);
            add((short)2);
            add((short)3);
            add((short)3);
            add((short)4);
            add((short)5);
            add((short)5);
            add((short)0);
            add((short)3);
        }};


        return order;
    }

    public Coordinate getTextCoord() {
        return textCoord;
    }

}
