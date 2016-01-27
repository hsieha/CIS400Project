package com.example.livelyturtle.androidar;

import java.util.ArrayList;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class WorldObject {
    String name;
    ArrayList<Double> coordinates;

    public WorldObject() {
        name = "";
        coordinates = new ArrayList<Double>();
    }

    public WorldObject(String name, ArrayList<Double> coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoordinates(ArrayList<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Double> getCoordinates() {
        return coordinates;
    }
}