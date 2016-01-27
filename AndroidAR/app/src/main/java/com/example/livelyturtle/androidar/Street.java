package com.example.livelyturtle.androidar;


import java.util.ArrayList;
/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class Street extends WorldObject {
    public Street(String name, double a, double b) {
        this.name = name;
        this.coordinates = new ArrayList<Double>();
        this.coordinates.add(a);
        this.coordinates.add(b);
    }

    public void setCoordinates(double a, double b) {
        this.coordinates = new ArrayList<Double>();
        this.coordinates.add(a);
        this.coordinates.add(b);
    }

    /// TODO: Darren, you can use this base to write your math code
    public boolean doesIntersect() {
        return false;
    }
}
