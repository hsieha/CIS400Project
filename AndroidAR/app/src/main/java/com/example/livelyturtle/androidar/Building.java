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
        super(name, coordinates, 6f);
        double lat = 0;
        double lon = 0;
        for (Coordinate coord : this.coordinates) {
            lat += coord.latitude;
            lon += coord.longitude;
        }
        lat -= this.coordinates.get(0).latitude;
        lon -= this.coordinates.get(0).longitude;
        // average of summation of all lat/long vals
        textCoord = new Coordinate(lat/(this.coordinates.size()-1),lon/(this.coordinates.size()-1));
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
        ArrayList<Short> order = new ArrayList<Short>();
        for (int i = 1; i <= (coordinates.size()*2)-2; i += 2) {
            order.add((short) i);
            order.add((short) (i-1));
            order.add((short) (i+1));
            order.add((short) i);
            order.add((short) (i+1));
            order.add((short) (i+2));
        }
        return order;
    }

    public Coordinate getTextCoord() {
        return textCoord;
    }

}
