package com.example.livelyturtle.androidar;

import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;
import java.util.ArrayList;

/**
 * Created by Darren on 2/20/16.
 */
public class Beacon extends WorldObject{

    static final int INDEX_COUNT = 240;
    static final int VERTEX_COUNT = 80;

    //detection radius around the beacon
    //triggers next event if within the detect radius
    //radius of the beacon itself is 1 meter
    double detect_radius = 5 + 1;

    Coordinate textCoord;

    double x;
    double z;

    //NOTE: Arraylist coordinates only holds 1 coordinate
    public Beacon(String name, ArrayList<Coordinate> coordinates){
        super(name, coordinates, 64.0f);

        double lat = 0;
        double lon = 0;
        for (Coordinate coord : this.coordinates) {
            lat += coord.latitude;
            lon += coord.longitude;
        }
        lat -= this.coordinates.get(0).latitude;
        lon -= this.coordinates.get(0).longitude;

        x = this.coordinates.get(0).x;
        z = this.coordinates.get(0).z;
    }

    public ArrayList<Moverio3D.Vector> vectors() {

        ArrayList<Moverio3D.Vector> vectors = new ArrayList<Moverio3D.Vector>();

        //Store top cap vertices (vertex 0-19)
        for(int i = 0; i < 20; i++){
            Vector v = Moverio3D.rotateYAxis(Vector.of(1f, (float) height, 0), (float) Math.toRadians(i * 18.0f));
            vectors.add(Vector.sum(v, Vector.of((float)x,0,(float)z)));
        }

        //Store bottom cap vertices (vertex 20-39)
        for(int i = 20; i < 40; i++){
            Vector v = Moverio3D.rotateYAxis(Vector.of(1f, 0, 0), (float) Math.toRadians((i - 20.0f) * 18.0f));
            vectors.add(Vector.sum(v, Vector.of((float) x, 0, (float) z)));
        }

        //Store top cap vertices (vertex 40-59)
        for(int i = 0; i < 20; i++){
            Vector v = Moverio3D.rotateYAxis(Vector.of(1f, (float) height, 0), (float) Math.toRadians(i * 18.0f));
            vectors.add(Vector.sum(v, Vector.of((float) x, 0, (float) z)));
        }

        //Store bottom cap vertices (vertex 60-79)
        for(int i = 20; i < 40; i++){
            Vector v = Moverio3D.rotateYAxis(Vector.of(1f, 0, 0), (float) Math.toRadians((i - 20.0f) * 18.0f));
            vectors.add(Vector.sum(v, Vector.of((float) x, 0, (float) z)));
        }

        return vectors;
    }

    //order of vertices
    public ArrayList<Short> vector_order(){

        ArrayList<Short> order = new ArrayList<Short>();

        //top cap indices, 0-53
        for(int i = 0; i < 18; i++){
            order.add((short) 0);
            order.add((short) (i+1));
            order.add((short) (i+2));
        }

        //bottom cap indices, 54-107
        for(int i = 18; i < 36; i++){
            order.add((short) 20);
            order.add((short) (i+3));
            order.add((short) (i+4));
        }

        //indices for barrel of cylinder
        for(int i = 0; i < 19; i++){
            order.add((short) (i+40));
            order.add((short) (i+41));
            order.add((short) (i+60));
            order.add((short) (i+41));
            order.add((short) (i+61));
            order.add((short) (i+60));
        }

        //build last quad of barrel, which has looping indices
        order.add((short)59);
        order.add((short)40);
        order.add((short)79);
        order.add((short)40);
        order.add((short)60);
        order.add((short)79);

        return order;
    }

    public Coordinate getTextCoord() {
        return textCoord;
    }

    //detects if given vector is inside radius of beacon's detection circle
    //if inside return true, else return false
    public boolean hasArrived(Vector eye_vector){
        if( Math.pow((eye_vector.x() - this.x), 2.0) + Math.pow((eye_vector.z() - this.z), 2.0) < Math.pow(detect_radius, 2.0)){
            return true;
        }
        return false;
    }

}
