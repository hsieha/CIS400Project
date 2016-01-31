package com.example.livelyturtle.androidar;

import java.util.ArrayList;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class WorldObject {
    String name;
    ArrayList<Coordinate> coordinates;

    public WorldObject() {
        name = "";
        coordinates = new ArrayList<Coordinate>();
    }

    public WorldObject(String name, ArrayList<Coordinate> coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoordinates(ArrayList<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Coordinate> getCoordinates() {
        return coordinates;
    }
}