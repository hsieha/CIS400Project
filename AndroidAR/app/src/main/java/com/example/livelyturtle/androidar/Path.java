package com.example.livelyturtle.androidar;

import java.util.ArrayList;

/**
 * Created by LivelyTurtle on 3/28/2016.
 */
public class Path extends Street {
    public Path(String name, ArrayList<Coordinate> coordinates) {
        super(name, coordinates, 0.1f);
    }
}
