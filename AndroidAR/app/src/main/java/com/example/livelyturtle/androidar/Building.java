package com.example.livelyturtle.androidar;

import java.util.ArrayList;
import java.util.Collections;

import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class Building extends WorldObject {

    Coordinate textCoord;

    public Building(String name, ArrayList<Coordinate> coordinates) {
        super(name, coordinates, 2.5f);
        double lat = 0;
        double lon = 0;
        for (Coordinate coord : this.coordinates) {
            lat += coord.latitude;
            lon += coord.longitude;
        }
        // average of summation of all lat/long vals
        textCoord = new Coordinate(lat/this.coordinates.size(),lon/this.coordinates.size());
    }

    //coordinates are held in counterclockwise order
    public ArrayList<Vector> vectors(){

        ArrayList<Vector> vectors = new ArrayList<Vector>();

        for(int i = 0; i < coordinates.size(); i++){
            //point given
            vectors.add(Vector.of((float)(coordinates.get(i).x), 0, (float)(coordinates.get(i).z)));
            //point in the air
            vectors.add(Vector.of((float)(coordinates.get(i).x), (float)(height), (float)(coordinates.get(i).z)));
        }

        return vectors;
    }

    //order of vertices
    public ArrayList<Short> vector_order(){

        ArrayList<Short> order = new ArrayList<Short>(Collections.nCopies(coordinates.size()*2*3,(short) 0));

        for(int i = 0; i < coordinates.size()*2-2; i++){
            order.set(i*3,(short) i);
            order.set(i*3 + 1,(short) (i+1));
            order.set(i*3 + 2,(short) (i+2));
        }
        //last 2 triangles double back onto first two vertices
        int j = coordinates.size()*2*3;
        order.set(j-6, (short) (coordinates.size()*2-2));
        order.set(j-5, (short) (coordinates.size()*2-1));
        order.set(j-4, (short) 0);
        order.set(j-3, (short) (coordinates.size()*2-1));
        order.set(j-2, (short) 0);
        order.set(j-1, (short) 1);

        return order;
    }

    public Coordinate getTextCoord() {
        return textCoord;
    }

}
