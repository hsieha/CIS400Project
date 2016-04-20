package com.example.livelyturtle.androidar.MoverioLibraries;

import android.content.Context;

import com.example.livelyturtle.androidar.Coordinate;
import com.example.livelyturtle.androidar.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Michael on 2/8/2016.
 *
 * Flags &c for debugging
 */
public final class DataDebug {

    private DataDebug() {}

    // change these as needed
    public static final LocationMode LOCATION_MODE  = LocationMode.REAL;
    // noise parameters are for the PATH_SIMULATION mode only
    public static final float NOISE_RADIUS          = 0f; // meters. noise if positive, else no noise
    public static final long NOISE_PERIOD           = 2500; // ms. invalid unless positive

    public static final long PATH_SIM_UPDATE_PERIOD = 200; // ms. set to 1 for complete smoothness.


    /*
     * compass coordinates (0,0):           39.952258   ,   -75.197008
     * middle of hamilton village:          39.952746   ,   -75.2009
     *
     */
    public static final double HARDCODE_LAT = 39.952746;
    public static final double HARDCODE_LONG = -75.2009;



    // *********************************************************************************************
    // *********************************************************************************************
    // *** LOGIC IS BELOW. DO NOT CHANGE THE CODE BELOW FOR DEBUG PURPOSES. ************************
    // *** (Obviously you can change it if you are developing it!) *********************************
    // *********************************************************************************************
    // *********************************************************************************************



    // HARDCODE: use the specified LAT and LONG above
    // REAL: use data from ExternalGPSReceiver... or just get placed at (0,0) if data not available
    // PATH_SIMULATION: read data from text file (res/raw/pathsimulation)
    public enum LocationMode {
        HARDCODE,
        REAL,
        PATH_SIMULATION
    }

    private static boolean pathTimerStarted = false;
    private static long startTime;
    private static List<Float> xlist = new ArrayList<>();
    private static List<Float> zlist = new ArrayList<>();
    private static List<Float> tlist = new ArrayList<>();

    /**
     * Returns true if successful. This method can only be called ONE time during a full application run.
     */
    public static boolean startPathTimer(Context ctxt) {
        if (!pathTimerStarted) {
            // read in path simulation file (res/raw/pathsimulation)
            // ignore "//" lines and empty lines
            // return false on IOException, or any formatting failure (non-monotonic times, etc)

            final InputStream inputStream = ctxt.getResources().openRawResource(R.raw.pathsimulation);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            int linenumber = 0;
            try {
                boolean done = false;
                while (!done) {
                    final String line = reader.readLine();
                    linenumber++;
                    done = (line == null);

                    if (line != null) {
                        if (line.trim().isEmpty() || line.trim().startsWith("//")) {
                            continue;
                        }
                        else {
                            String[] vals = line.split(",");
                            if (vals.length != 3) {
                                System.out.println("(!) pathsimulation bad line formatting: " + linenumber);
                                return false;///////////////////////////////////////////////////////
                            }
                            float x = Float.parseFloat(vals[0]);
                            float z = Float.parseFloat(vals[1]);
                            float t = Float.parseFloat(vals[2]);

                            if (!tlist.isEmpty() && tlist.get(tlist.size() - 1) >= t) {
                                System.out.println("(!) pathsimulation times not monotonically increasing: " + linenumber);
                                return false;///////////////////////////////////////////////////////
                            }

                            xlist.add(x);
                            zlist.add(z);
                            tlist.add(t);
                        }
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return false;///////////////////////////////////////////////////////////////////////
            } catch (NumberFormatException nfe) {
                System.out.println("(!) pathsimulation contains an unparsable value: " + linenumber);
                return false;///////////////////////////////////////////////////////////////////////
            }

            if (xlist.size() == 0) {
                System.out.println("(!) pathsimulation has no data");
                return false;///////////////////////////////////////////////////////////////////////
            }

            startTime = System.currentTimeMillis();
            return (pathTimerStarted = true);
        }
        return false;
    }

    /**
     * Returns null if the pathTimerStarted hasn't been toggled yet.
     *
     * If noise is turned on, think of it as a "noise-generating machine" whose output is added to the specified
     * path simulation coordinate. (If we should be at (10, 10) at time t=60s, and the noise machine
     * generates (-.2, .3), then the actual returned value will be (9.8, 10.3).)
     * NOISE_PERIOD would determine how long (-.2, .3) stays effective for. After it becomes ineffective,
     * the machine generates a new noise value.
     *
     * NOTE that the noise is NOT equally distributed through the circle of all possible values. It
     * is more likely to be closer to the center. (mathworld.wolfram.com/DiskPointPicking.html)
     */
    private static int currI = 0;
    private static double[] rands = {0,0};
    private static long lastTimeMultiple = 0;
    public static Coordinate getPathSimulationCoordinate() {
        if (pathTimerStarted) {
            long now = System.currentTimeMillis();
            float passed = (now - startTime) / 1000.f; // in seconds

            // calculate Coordinate from the 3 lists
            int len = xlist.size();
            // currI refers to the FIRST index with time >= passed
            while (currI < len && tlist.get(currI) < passed) currI++;

            Coordinate result;
            if (currI == 0) result = Coordinate.fromXZ(xlist.get(0), zlist.get(0));
            else if (currI == len) result = Coordinate.fromXZ(xlist.get(len-1), zlist.get(len-1));
            else {
                float stepCompletion = (passed - tlist.get(currI-1)) /
                                       (tlist.get(currI) - tlist.get(currI-1));
                float xAdj = stepCompletion * (xlist.get(currI) - xlist.get(currI-1));
                float zAdj = stepCompletion * (zlist.get(currI) - zlist.get(currI-1));
                result = Coordinate.fromXZ(xlist.get(currI-1) + xAdj, zlist.get(currI-1) + zAdj);
            }

            // add noise if necessary
            if (NOISE_PERIOD > 0 && NOISE_RADIUS > 0) {
                // this if ensures rands are only updated according to NOISE_PERIOD
                if (lastTimeMultiple != now/NOISE_PERIOD) {
                    lastTimeMultiple = now/NOISE_PERIOD;
                    rands[0] = Math.random();
                    rands[1] = Math.random();
                }
                // polar coordinates
                double radius = rands[0] * NOISE_RADIUS;
                double angle = rands[1] * Math.PI * 2;
                Coordinate noise = Coordinate.fromXZ(Math.cos(angle) * radius, Math.sin(angle) * radius);
                // I did not use Coordinate.add because that method uses lat/long values, not x/z
                result = Coordinate.fromXZ(result.x + noise.x, result.z + noise.z);
            }

            return result;
        }
        return null;
    }

}
