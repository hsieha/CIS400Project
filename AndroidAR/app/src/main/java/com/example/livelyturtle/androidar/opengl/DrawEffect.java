package com.example.livelyturtle.androidar.opengl;

import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.Vector;



/**
 * Visual effects.
 * Currently there is no support for combining effects.
 */
interface DrawEffect {

    boolean isVisible();
    Vector getModifiedColor(Vector original);

    /**
     * Visibility effect.
     */
    class Blink implements DrawEffect {

        // milliseconds
        long absStartT;     // base time things are measured against; use to change phase
        long period;        // amount of time between the start of each beginVisible
        long beginVisible;  // instant of time (< period) within each period when the drawing should be visible
        long endVisible;    // instant of time (<= period) within each period when the drawing should not be visible.

    /*
    EXAMPLE:
    Suppose I want 2 alternating lights like at a railroad crossing, with a period of 1000ms.
    LIGHT1: Blink(0,1000,0,500)
    LIGHT2: Blink(0,1000,500,1000)
    LIGHT1, in each period, is visible from the beginning of time t=0 until the beginning of time t=500.
    This means LIGHT1 is drawn from t=0 until t=499.99999... (which is exactly 500ms).
    LIGHT2 is visible from t=500 to t=999.99999...

    The same effect can be achieved with:
    LIGHT1: Blink(0,1000,0,500)
    LIGHT2: Blink(500,1000,0,500)
    The visible period is defined the same way, but LIGHT2 is out of phase by 500ms, as specified by the
    first argument to the constructor.
     */

        public Blink(long absStartT, long period, long beginVisible, long endVisible) {
            this.absStartT      = absStartT;
            this.period         = period;
            this.beginVisible   = beginVisible;
            this.endVisible     = endVisible;
        }

        @Override
        public boolean isVisible() {
            long curr = System.currentTimeMillis();
            long advancement = (curr - absStartT) % period;
            return (advancement >= beginVisible) && advancement < (endVisible);
        }

        @Override
        public Vector getModifiedColor(Vector original) {
            return original;
        }

    }

    /**
     * Color effect. Sine wave.
     */
    class Throb implements DrawEffect {

        Vector otherColor;  // the two colors used are: the original used to construct the DrawExecutor; and this one
        long period;        // milliseconds

    /*
    EXAMPLE:
    This effect sets up a sine wave with specified period for EACH of the 3 color components (opacity
    is unchanged).
    For example, if original color is (1,.5,0) and other color is (0,.7,1), with a period of 1000ms,
    then at time t=0 you see original color, at time t=500 you see other color, and at t=1000 you
    see original color again. Red goes 1 to 0 to 1; Blue goes .5 to .7 to .5; Green goes 0 to 1 to 0.
     */

        public Throb(Vector otherColor, long period) {
            this.otherColor = otherColor;
            this.period     = period;
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public Vector getModifiedColor(Vector original) {
            // A sin(Bx) + D
            // A is orig-other
            // B is 2pi/T
            // D is the (orig+other)/2
            long x = System.currentTimeMillis();
            float modR = (float)
                    ((original.x()-otherColor.x()) *
                            Math.sin(2.f*Math.PI/period * x) +
                            ((original.x()+otherColor.x())/2.f));
            float modG = (float)
                    ((original.y()-otherColor.y()) *
                            Math.sin(2.f*Math.PI/period * x) +
                            ((original.y()+otherColor.y())/2.f));
            float modB = (float)
                    ((original.z()-otherColor.z()) *
                            Math.sin(2.f*Math.PI/period * x) +
                            ((original.z()+otherColor.z())/2.f));
            return Vector.of(modR, modG, modB);
        }

    }

}


class DefaultEffect implements DrawEffect {

    static DrawEffect DEFAULT_EFFECT = new DefaultEffect();

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public Vector getModifiedColor(Vector original) {
        return original;
    }

}








