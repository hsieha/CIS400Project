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

}
