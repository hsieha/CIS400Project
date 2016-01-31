package com.example.livelyturtle.androidar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import android.app.Activity;

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

    // Parsing a .kml file from Google Maps
    public MapData(String filename, Activity activity) {
        String line = null;
        buildings = new HashSet<Building>();
        streets = new HashSet<Street>();
        try {
            InputStream iS = activity.getAssets().open("UPennCampus.kml");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(iS));
            boolean isBuilding = true; // true if adding a building, false if adding a street
            String name = "";
            ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
            while((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                System.out.println(line);
                if(line.equals("<name>Buildings</name>")) {
                    isBuilding = true;
                }
                else if(line.equals("<name>Streets</name>")) {
                    isBuilding = false;
                }
                else if(line.startsWith("<name>")) {
                    name = line.substring(6, line.length() - 7);
                }
                else if(line.startsWith("<coordinates>")) {
                    coordinates = new ArrayList<Coordinate>();
                    String coordString = line.substring(13, line.length() - 14);
                    for(String oneCoord : coordString.split(",0.0 ")){
                        String[] latlong = oneCoord.split(",");
                        Coordinate coord = new Coordinate(Double.parseDouble(latlong[0]),
                                Double.parseDouble(latlong[1]));
                        coordinates.add(coord);
                    }
                    if(isBuilding) {
                        buildings.add(new Building(name,coordinates));
                    }
                    else {
                        streets.add(new Street(name, coordinates));
                    }
                }
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to find " + filename);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
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
