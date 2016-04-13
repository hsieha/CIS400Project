package com.example.livelyturtle.androidar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class Building extends WorldObject {

    Coordinate textCoord;
    Vector color;
    private Random myR = new Random();

    public Building(String name, ArrayList<Coordinate> coordinates) {
        super(name, coordinates, 6f);
        double lat = 0;
        double lon = 0;
        for (Coordinate coord : this.coordinates) {
            lat += coord.latitude;
            lon += coord.longitude;
        }
        // Michael: I think we remove the first coord from this calculation because in the kml file,
        // the first and last coordinates are repeats (to properly store a closed curve).
        lat -= this.coordinates.get(0).latitude;
        lon -= this.coordinates.get(0).longitude;
        // average of summation of all lat/long vals
        textCoord = new Coordinate(lat/(this.coordinates.size()-1),lon/(this.coordinates.size()-1));

        color = getColorAlgorithm();
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
    public Vector getColor() { return color; }

    /*
    The idea behind this complication is to avoid having randomly generated colors tend toward gray
    (or black/white). We want colorful buildings that stand out from their surroundings.
     */
    private Vector getColorAlgorithm() {
        double base = .20 + .20 * Math.random();
        double adj1 = .40 + .20 * Math.random();
        double adj2 = .15 + .15 * Math.random();
        double adj3 = .05 + .10 * Math.random();

        switch (myR.nextInt(6)) {
            case 0:
                return Vector.of(base+adj1, base+adj2, base+adj3);
            case 1:
                return Vector.of(base+adj1, base+adj3, base+adj2);
            case 2:
                return Vector.of(base+adj2, base+adj1, base+adj3);
            case 3:
                return Vector.of(base+adj2, base+adj3, base+adj1);
            case 4:
                return Vector.of(base+adj3, base+adj1, base+adj2);
            case 5:
                return Vector.of(base+adj3, base+adj2, base+adj1);
            default: // pretty obvious this shouldn't happen
                return Vector.of(1,1,1);
        }
    }

}
