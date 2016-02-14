package com.example.livelyturtle.androidar;

import org.junit.Test;
import org.junit.Assert;

public class CoordinateTest {
    @Test
    public void testCoordinateAtCompass() {
        double epsilon = 0.01; // a centimeter
        Coordinate c = new Coordinate(Coordinate.COMPASS_LAT, Coordinate.COMPASS_LONG);
        Assert.assertTrue(c.x <  epsilon);
        Assert.assertTrue(c.x > -epsilon);
        Assert.assertTrue(c.z <  epsilon);
        Assert.assertTrue(c.z > -epsilon);
    }
}
