package com.example.livelyturtle.androidar.MoverioLibraries;

/**
 * Created by Michael on 2/8/2016.
 *
 * Flags for debugging
 */
public final class DataDebug {

    private DataDebug() {}

    // if you set HARDCODE to false, then real GPS data will be used
    // if such data isn't available, the user is placed at (0,0,0) - the Compass at 37th and Locust
    // text will display showing that World3DActivity did not receive location data
    public static final boolean HARDCODE_LOCATION = true;
    /*
     * compass coordinates (0,0):           39.952258   ,   -75.197008
     * middle of hamilton village:          39.952746   ,   -75.2009
     *
     */

    public static final double HARDCODE_LAT = 39.952746;
    public static final double HARDCODE_LONG = -75.2009;


}
