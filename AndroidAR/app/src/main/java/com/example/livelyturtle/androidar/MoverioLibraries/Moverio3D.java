package com.example.livelyturtle.androidar.MoverioLibraries;

/**
 * Created by Michael on 1/23/2016.
 *
 * "north" is -Z
 * "south" is +Z
 * "east" is +X
 * "west" is -X
 */
public class Moverio3D {

    // like Math, this class is a support library and cannot be instantiated.
    private Moverio3D() {}

    public static class Vector {
        private float x, y, z;
        private Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
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



}
