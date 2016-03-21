package com.example.livelyturtle.androidar;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GenerateXZUtility {

    // Use Google Earth. In Tools > Options, change "Show Lat/Long" to "Decimal Degrees".
    // Move your mouse to the desired location to get lat/long coordinates.
    // Enter the coordinates into the list.
    // Run this test file (right-click in the left sidebar, and choose "Run"), and the associated
    // XZ meter coordinates will be output in "Android Monitor".

    List<LL> list = new LinkedList<LL>(){{
        add(new LL(Coordinate.COMPASS_LAT, Coordinate.COMPASS_LONG));
        add(new LL(39.96, -75.20));
        // etc...

    }};











    @Test
    public void outputXZ() {
        Log.d(s, "===== BEGIN GENERATE_XZ_UTILITY =====");
        int current = 1;
        for (LL ll : list) {
            Coordinate c = new Coordinate(ll.lat, ll.lon);
            Log.d(s, String.format("%3d", current++) + ":\t" +
                     String.format("%10.3f",c.x) + "\t" +
                     String.format("%10.3f",c.z));
        }
        Log.d(s, "===== END GENERATE_XZ_UTILITY =====");
    }

    static class LL {
        double lat;
        double lon;
        LL(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    String s = "GenerateXZUtility";

}
