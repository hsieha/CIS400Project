package com.example.livelyturtle.androidar.MoverioLibraries;

import java.text.Format;
import java.util.List;

/**
 * Created by Michael on 1/23/2016.
 *
 * This is a utilities class for the Moverio BT-200 smartglasses.
 *
 * Most units of length will be in meters.
 *
 * "north" is -Z
 * "south" is +Z
 * "east" is +X
 * "west" is -X
 */
public final class Moverio3D {

    // like Math, this class is a support library and cannot be instantiated.
    private Moverio3D() {}

    // constants
    public static final int SCREEN_WIDTH                = 960;          // pixels
    public static final int SCREEN_HEIGHT               = 540;          // pixels
    public static final float VIRTUAL_SCREEN_SIZE       = 2.032f;       // m ("80in" in manual)
    public static final float VIRTUAL_SCREEN_DISTANCE   = 5.f;          // m (in manual)
    public static final float VIRTUAL_SCREEN_WIDTH      = 1.771041f;    // m (pythagorean theorem)
    public static final float VIRTUAL_SCREEN_HEIGHT     = .996211f;     // m (pythagorean theorem)
    public static final float FOVX                      = .350573117f;  // radians (about 20 degrees)
    public static final float FOVY                      = .198586948f;  // radians (about 11 degrees)

    public static final String SCREEN_DENSITY           = "mdpi";       // (160dpi)
    public static final String PROCESSOR                = "OMAP4460";   // (dual core ARM Cortex A9)


    public static class Vector {
        private float x, y, z;
        private Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        // print
        @Override
        public String toString() {
            return "(" + String.format("%9.2f", x) + ", " +
                         String.format("%9.2f", y) + ", " +
                         String.format("%9.2f", z) + ")";
        }

        // static factories
        public static Vector zero() {
            return new Vector(0,0,0);
        }
        public static Vector of(float x, float y, float z) {
            return new Vector(x,y,z);
        }

        // accessors
        public float x() {
            return x;
        }
        public float y() {
            return y;
        }
        public float z() {
            return z;
        }
        public float[] xyz() { return new float[] {x,y,z}; }

        // useful methods
        public Vector scalarMultiply(float s) {
            return of(s*x, s*y, s*z);
        }
        public float magnitude() {
            return (float) Math.sqrt(x*x + y*y + z*z);
        }
        public Vector normalized() {
            return this.scalarMultiply(1.f/this.magnitude());
        }

        // static methods
        public static Vector sum(Vector addend1, Vector addend2) {
            return of(addend1.x + addend2.x, addend1.y + addend2.y, addend1.z + addend2.z);
        }
        public static Vector difference(Vector end, Vector start) {
            return of(end.x - start.x, end.y - start.y, end.z - start.z);
        }

        // getting float arrays
        // TODO: a candidate for parallelization with Java 8?
        public static float[] vectorsToFloatArray(Vector... args) {
            float[] result = new float[args.length*3];
            int i = 0;
            for (Vector v : args) {
                for (float f : v.xyz()) {
                    result[i++] = f;
                }
            }
            return result;
        }
        public static float[] vectorsToFloatArray(List<Vector> args) {
            float[] result = new float[args.size()*3];
            int i = 0;
            for (Vector v : args) {
                for (float f : v.xyz()) {
                    result[i++] = f;
                }
            }
            return result;
        }
    }

    public enum CardinalDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NORTHWEST,
        NORTHEAST,
        SOUTHWEST,
        SOUTHEAST
    }

    // rotate around the Y axis.
    // positive is counterclockwise, looking from +Y (above)
    public static Vector rotateYAxis(Vector in, float rads) {
        return Vector.of(
                (float) (in.z * Math.sin(rads) + in.x * Math.cos(rads)),
                (in.y),
                (float) (in.z * Math.cos(rads) - in.x * Math.sin(rads))
        );
    }

    public static Vector rotateW(Vector in, float rads) {
        return rotateYAxis(in, rads);
    }

    public static Vector rotateE(Vector in, float rads) {
        return rotateYAxis(in, -1.f * rads);
    }

    /**
     * This uses the default azimuth from the APR value, NOT the negated one necessary for matrix
     * calculations
     * @param a APR[0]
     * @return direction
     */
    public static CardinalDirection getDirectionFromAzimuth(float a) {

        int aDeg = (int) (a * 180 / (float) Math.PI);

        // adjustments to make the following switch statement easier
        if (aDeg < 0) aDeg += 360;
        aDeg += 22;

        switch (aDeg/45) {
            case 0:
            case 8:
                return CardinalDirection.NORTH;
                //break;
            case 1:
                return CardinalDirection.NORTHEAST;
                //break;
            case 2:
                return CardinalDirection.EAST;
                //break;
            case 3:
                return CardinalDirection.SOUTHEAST;
                //break;
            case 4:
                return CardinalDirection.SOUTH;
                //break;
            case 5:
                return CardinalDirection.SOUTHWEST;
                //break;
            case 6:
                return CardinalDirection.WEST;
                //break;
            case 7:
                return CardinalDirection.NORTHWEST;
                //break;
            default:
                // error
                return null;
                //break;
        }
    }

}
