package com.example.livelyturtle.androidar;


import java.util.ArrayList;
/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class Street extends WorldObject {
    public Street(String name, double a, double b, double c, double d) {
        this.name = name;
        setCoordinates(a,b,c,d);
    }

    public Street(String name, ArrayList<Coordinate> coordinates) {
        super(name,coordinates);
    }

    public void setCoordinates(double a, double b, double c, double d) {
        Coordinate c1 = new Coordinate(a,b);
        Coordinate c2 = new Coordinate(c,d);
        this.coordinates = new ArrayList<Coordinate>();
        this.coordinates.add(c1);
        this.coordinates.add(c2);
    }

    /// TODO: Darren, you can use this base to write your math code
    public boolean doesIntersect() {
        return false;
    }
}
