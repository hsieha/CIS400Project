package com.example.livelyturtle.androidar;

import java.util.ArrayList;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class Building extends WorldObject {
    public Building(String name, ArrayList<Coordinate> coordinates) {
        super(name, coordinates);
    }

    /// TODO: Darren, you can use this base to write your math code
    public boolean doesIntersect() {
        return false;
    }
}
