package com.example.livelyturtle.androidar;

import android.location.Location;
import java.util.ArrayList;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class WorldObject {
    String name;
    ArrayList<Location> coordinates;

    public WorldObject() {
        name = "";
        coordinates = new ArrayList<Location>();
    }

    public WorldObject(String name, ArrayList<Location> coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoordinates(ArrayList<Location> coordinates) {
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Location> getCoordinates() {
        return coordinates;
    }
}