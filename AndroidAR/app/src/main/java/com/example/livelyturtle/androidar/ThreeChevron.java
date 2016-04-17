package com.example.livelyturtle.androidar;

import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;
import java.util.ArrayList;

import com.example.livelyturtle.androidar.opengl.DrawEffect;
import com.example.livelyturtle.androidar.opengl.MyGLRenderer;

/**
 * Created by somesingman on 3/20/16.
 */
public class ThreeChevron extends WorldObject{

    Coordinate textCoord;
    float angle;
    double lat;
    double lon;

    //distance from each chevron in meters
    double dist = 3;

    public Chevron chev_one;
    public Chevron chev_two;
    public Chevron chev_three;

    //NOTE: Arraylist coordinates only holds 1 coordinate
    //this coordinate is the top center vertex of the first(closest) chevron
    //must also enter a direction to point the chevron
    //direction given in degrees from north clockwise
    public ThreeChevron(String name, ArrayList<Coordinate> coordinates, float dir){
        super(name, coordinates, 0.3f);

        for (Coordinate coord : this.coordinates) {
            this.lat += coord.latitude;
            this.lon += coord.longitude;
        }

        angle = dir;

        if (Math.abs(angle) > 360){
            angle = angle % 360;
        }

        //first chevron
        ArrayList<Coordinate> coor1 = new ArrayList<Coordinate>();
        coor1.add(new Coordinate(lat, lon));
        chev_one = new Chevron("one", coor1, angle);

        //second chevron
        ArrayList<Coordinate> coor2 = new ArrayList<Coordinate>();
        coor2.add(new Coordinate(lat, lon));
        chev_two = new Chevron("two", coor2, angle);
        chev_two.set_x(chev_two.x + dist*Math.cos(-Math.toRadians(angle+90)));
        chev_two.set_z(chev_two.z + dist*Math.sin(-Math.toRadians(angle+90)));

        //third chevron
        ArrayList<Coordinate> coor3 = new ArrayList<Coordinate>();
        coor3.add(new Coordinate(lat, lon));
        chev_three = new Chevron("three", coor3, angle);
        chev_three.set_x(chev_three.x + 2*dist*Math.cos(-Math.toRadians(angle+90)));
        chev_three.set_z(chev_three.z + 2 * dist * Math.sin(-Math.toRadians(angle + 90)));

    }

    public Chevron[] chevron_list(){
        return new Chevron[] {chev_one, chev_two, chev_three};
    }

    //location of each vertex
    public ArrayList<ArrayList<Moverio3D.Vector>> vectors(){

        ArrayList<ArrayList<Moverio3D.Vector>> vectors_array = new ArrayList<ArrayList<Moverio3D.Vector>>(3);

        //add them to output
        vectors_array.add(chev_one.vectors());
        vectors_array.add(chev_two.vectors());
        vectors_array.add(chev_three.vectors());

        return vectors_array;
    }

    //order of vertices doesn't change between chevrons
    public ArrayList<Short> vector_order() {

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

}
