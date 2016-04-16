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
//        add(new LL(39.952556, -75.190040)); //595.436	   -33.111
//        add(new LL(39.952673, -75.190997)); //513.657	   -46.096
//        add(new LL(39.952812, -75.192101)); //419.316 ,     -61.524
//        add(new LL(39.952958, -75.202835)); // 40/L
//        add(new LL(39.951892, -75.194403)); // L/Wood
//        add(new LL(39.952788, -75.192113)); // 34/W
//        add(new LL(39.952323, -75.192182)); // 34/Chancellor

        //for tour
        add(new LL(39.952749, -75.192248)); // 34th/Walnut
        add(new LL(39.952155, -75.193684)); // Button
        add(new LL(39.952749, -75.192248)); // The Arch
        add(new LL(39.952148, -75.196047)); // Steiny D.
        add(new LL(39.952259, -75.197009)); // Compass + Ben on Bench
        add(new LL(39.952415, -75.198175)); // Huntsman
        add(new LL(39.952550, -75.199369)); // 1920 Commons
        add(new LL(39.952724, -75.200779)); // The Convenant (Dueling Tampons)

        //for demo
        add(new LL(39.951485, -75.190791));  // Smith Walk top of Stairs
        add(new LL(39.951609, -75.191695));  // Smith / Front of Town
        add(new LL(39.952249, -75.191561));  // Chancellor st / front of levine
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
