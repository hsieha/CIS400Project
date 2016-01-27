package com.example.livelyturtle.androidar;

import java.util.HashSet;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class MapData {
    HashSet<Building> buildings;
    HashSet<Street> streets;

    public MapData() {
        buildings = new HashSet<Building>();
        streets = new HashSet<Street>();
    }

    public void addData(WorldObject object) {
        if(object instanceof Building) {
            buildings.add((Building) object);
        }
        else if(object instanceof Street) {
            streets.add((Street) object);
        }
        else {
            throw new IllegalArgumentException("Object not of type Building or Street");
        }
    }

    public HashSet<Building> getBuildings() {
        return buildings;
    }

    public HashSet<Street> getStreets() {
        return streets;
    }
}
