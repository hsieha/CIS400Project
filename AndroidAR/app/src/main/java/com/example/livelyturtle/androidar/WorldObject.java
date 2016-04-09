package com.example.livelyturtle.androidar;

import java.util.ArrayList;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class WorldObject {
    String name;
    ArrayList<Coordinate> coordinates;
    double height;

    public WorldObject() {
        name = "";
        coordinates = new ArrayList<Coordinate>();
        height = 0;
    }

    public WorldObject(String name, ArrayList<Coordinate> coordinates, double height) {
        this.name = name;
        this.coordinates = coordinates;
        this.height = height;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoordinates(ArrayList<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Coordinate> getCoordinates() {
        return coordinates;
    }

    public double getHeight() { return height; }
}