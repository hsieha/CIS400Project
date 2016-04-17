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
    float angle;
    double x;
    double z;

    //NOTE: Arraylist coordinates only holds 1 coordinate
    //this coordinate is the top center vertex of the chevron
    //must also enter a direction to point the chevron
    //direction given in degrees from south clockwise
    public Chevron(String name, ArrayList<Coordinate> coordinates, float dir){
        super(name, coordinates, 0.3f);

        angle = dir;

        x = this.coordinates.get(0).x;
        z = this.coordinates.get(0).z;
    }

    public void set_x(double new_x){
        x = new_x;
    }
    public void set_z(double new_z){
        z = new_z;
    }

    //location of each vertex
    public ArrayList<Moverio3D.Vector> vectors() {

        ArrayList<Moverio3D.Vector> vectors = new ArrayList<Moverio3D.Vector>();

        //given point for top of the chevron
        vectors.add(Vector.of((float) x, (float) height, (float) z));

        //all other points are rotated based on specified direction given for where chevron is point at
        //chevron is being built counterclockwise

        //NOTE: need to test directions more heavily

        //vectors.add(Vector.sum((float) (x + 2.0), (float) height, (float) (z + 1)));
        Vector v1 = Moverio3D.rotateYAxis(Vector.of(-1.0f, (float) height, -1.0f), (float) Math.toRadians(angle));
        vectors.add(Vector.sum(v1, Vector.of((float) x, 0, (float) z)));

        //vectors.add(Vector.sum((float) (x + 2.0), (float) height, (float) (z + 2)));
        Vector v2 = Moverio3D.rotateYAxis(Vector.of(-1.0f, (float) height, 0.0f), (float) Math.toRadians(angle));
        vectors.add(Vector.sum(v2, Vector.of((float) x, 0, (float) z)));

        //vectors.add(Vector.sum((float) x, (float) height, (float) (z + 1)));
        Vector v3 = Moverio3D.rotateYAxis(Vector.of(0.0f, (float) height, 1.0f), (float) Math.toRadians(angle));
        vectors.add(Vector.sum(v3, Vector.of((float) x, 0, (float) z)));

        //vectors.add(Vector.sum((float) (x - 2.0), (float) height, (float) (z + 2)));
        Vector v4 = Moverio3D.rotateYAxis(Vector.of(1.0f, (float) height, 0.0f), (float) Math.toRadians(angle));
        vectors.add(Vector.sum(v4, Vector.of((float) x, 0, (float) z)));

        Vector v5 = Moverio3D.rotateYAxis(Vector.of(1.0f, (float) height, -1.0f), (float) Math.toRadians(angle));
        vectors.add(Vector.sum(v5, Vector.of((float) x, 0, (float) z)));

        return vectors;
    }

    //order of vertices
    public ArrayList<Short> vector_order() {

        //DARREN: couldn't figure out a smart way to iterate through this
        ArrayList<Short> order = new ArrayList<Short>() {{
            add((short) 0);
            add((short) 1);
            add((short) 2);
            add((short) 2);
            add((short) 3);
            add((short) 0);
            add((short) 0);
            add((short) 3);
            add((short) 4);
            add((short) 4);
            add((short) 5);
            add((short) 0);
        }};

        return order;
    }

    public Coordinate getTextCoord() {
        return textCoord;
    }

}
