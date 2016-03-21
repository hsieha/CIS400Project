package com.example.livelyturtle.androidar;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CoordinateTest {

    @Test
    public void testCoordinateAtCompass() {
        double epsilon = 0.01; // a centimeter
        Coordinate c = new Coordinate(Coordinate.COMPASS_LAT, Coordinate.COMPASS_LONG);
        Assert.assertEquals("x==0?", 0, c.x, epsilon);
        Assert.assertEquals("z==0?", 0, c.z, epsilon);
    }

    @Test
    public void testConstantDistanceAwayFromCompass() {
        double epsilon = 0.01; // a centimeter
        double someDist = 0.001; // about 110m?

        Coordinate cN = new Coordinate(Coordinate.COMPASS_LAT + someDist, Coordinate.COMPASS_LONG);
        Coordinate cNE = new Coordinate(Coordinate.COMPASS_LAT + someDist, Coordinate.COMPASS_LONG + someDist);
        Coordinate cE = new Coordinate(Coordinate.COMPASS_LAT, Coordinate.COMPASS_LONG + someDist);
        Coordinate cSE = new Coordinate(Coordinate.COMPASS_LAT - someDist, Coordinate.COMPASS_LONG + someDist);
        Coordinate cS = new Coordinate(Coordinate.COMPASS_LAT - someDist, Coordinate.COMPASS_LONG);
        Coordinate cSW = new Coordinate(Coordinate.COMPASS_LAT - someDist, Coordinate.COMPASS_LONG - someDist);
        Coordinate cW = new Coordinate(Coordinate.COMPASS_LAT, Coordinate.COMPASS_LONG - someDist);
        Coordinate cNW = new Coordinate(Coordinate.COMPASS_LAT + someDist, Coordinate.COMPASS_LONG - someDist);

        // N is around 0 different along x; negative along z
        Assert.assertEquals("N: x==0?", 0, cN.x, epsilon);
        Assert.assertTrue(cN.z < 0);

        // NE is +x, -z
        Assert.assertTrue(cNE.x > 0);
        Assert.assertTrue(cNE.z < 0);

        // E is +x
        Assert.assertTrue(cE.x > 0);
        Assert.assertEquals("E: z==0?", 0, cE.z, epsilon);

        // SE is +x, +z
        Assert.assertTrue(cSE.x > 0);
        Assert.assertTrue(cSE.z > 0);

        // S is +z
        Assert.assertEquals("S: x==0?", 0, cS.x, epsilon);
        Assert.assertTrue(cS.z > 0);

        // SW is -x, +z
        Assert.assertTrue(cSW.x < 0);
        Assert.assertTrue(cSW.z > 0);

        // W is -x
        Assert.assertTrue(cW.x < 0);
        Assert.assertEquals("W: z==0?", 0, cW.z, epsilon);

        // NW is -x, -z
        Assert.assertTrue(cNW.x < 0);
        Assert.assertTrue(cNW.z < 0);

    }

    @Test
    public void convertMetersToLatLong() {
        double epsilon = 1.; // allow for 1m of inaccuracy

        // these tests convert an x/z format into lat/long format, then
        // convert BACK into x/z format (the "new" Coordinates).
        // Since the 2 conversions are handled with entirely separate algorithms, the idea is
        // to ensure that the values do not stray too far.

        // some coordinates
        Coordinate cN = Coordinate.fromXZ(0,-100);
        Coordinate cNE = Coordinate.fromXZ(50,-50);
        Coordinate cE = Coordinate.fromXZ(100,0);
        Coordinate cSE = Coordinate.fromXZ(50,50);
        Coordinate cS = Coordinate.fromXZ(0,100);
        Coordinate cSW = Coordinate.fromXZ(-50,50);
        Coordinate cW = Coordinate.fromXZ(-100,0);
        Coordinate cNW = Coordinate.fromXZ(-50,-50);

        Coordinate newN = new Coordinate(cN.latitude, cN.longitude);
        Coordinate newNE = new Coordinate(cNE.latitude, cNE.longitude);
        Coordinate newE = new Coordinate(cE.latitude, cE.longitude);
        Coordinate newSE = new Coordinate(cSE.latitude, cSE.longitude);
        Coordinate newS = new Coordinate(cS.latitude, cS.longitude);
        Coordinate newSW = new Coordinate(cSW.latitude, cSW.longitude);
        Coordinate newW = new Coordinate(cW.latitude, cW.longitude);
        Coordinate newNW = new Coordinate(cNW.latitude, cNW.longitude);

        Assert.assertEquals("N x value correct", cN.x, newN.x, epsilon);
        Assert.assertEquals("N z value correct", cN.z, newN.z, epsilon);

        Assert.assertEquals("NE x value correct", cNE.x, newNE.x, epsilon);
        Assert.assertEquals("NE z value correct", cNE.z, newNE.z, epsilon);

        Assert.assertEquals("E x value correct", cE.x, newE.x, epsilon);
        Assert.assertEquals("E z value correct", cE.z, newE.z, epsilon);

        Assert.assertEquals("SE x value correct", cSE.x, newSE.x, epsilon);
        Assert.assertEquals("SE z value correct", cSE.z, newSE.z, epsilon);

        Assert.assertEquals("S x value correct", cS.x, newS.x, epsilon);
        Assert.assertEquals("S z value correct", cS.z, newS.z, epsilon);

        Assert.assertEquals("SW x value correct", cSW.x, newSW.x, epsilon);
        Assert.assertEquals("SW z value correct", cSW.z, newSW.z, epsilon);

        Assert.assertEquals("W x value correct", cW.x, newW.x, epsilon);
        Assert.assertEquals("W z value correct", cW.z, newW.z, epsilon);

        Assert.assertEquals("NW x value correct", cNW.x, newNW.x, epsilon);
        Assert.assertEquals("NW z value correct", cNW.z, newNW.z, epsilon);
    }

}
