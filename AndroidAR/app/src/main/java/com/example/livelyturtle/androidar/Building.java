package com.example.livelyturtle.androidar;

import java.util.ArrayList;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class Building extends WorldObject {

    private float y = 2.5f;  //All buildings are 2.5 meters tall (for now)

    public Building(String name, ArrayList<Coordinate> coordinates) {
        super(name, coordinates, 20);
    }

    //coordinates are held in counterclockwise order
    public ArrayList<Vector> vectors(){

        ArrayList<Vector> vectors = new ArrayList<Vector>();

        for(int i = 0; i < coordinates.size(); i++){
            //point given
            vectors.add(Vector.of((float)(coordinates.get(i).x), 0, (float)(coordinates.get(i).z)));
            //point in the air
            vectors.add(Vector.of((float)(coordinates.get(i).x), y, (float)(coordinates.get(i).z)));
        }

        return vectors;
    }

    //order of vertices
    public int[] vector_order(){

        int[] order = new int[coordinates.size()*2*3];

        for(int i = 0; i < coordinates.size()*2-2; i++){
            order[i*3] = i;
            order[i*3 + 1] = i+1;
            order[i*3 + 2] = i+2;
        }
        //last 2 triangles double back onto first two vertices
        int j = coordinates.size()*2*3;
        order[j-6] = coordinates.size()*2-2;
        order[j-5] = coordinates.size()*2-1;
        order[j-4] = 0;
        order[j-3] = coordinates.size()*2-1;
        order[j-2] = 0;
        order[j-1] = 1;

        return order;
    }

}
