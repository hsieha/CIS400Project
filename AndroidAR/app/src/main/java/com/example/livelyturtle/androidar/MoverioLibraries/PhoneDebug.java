package com.example.livelyturtle.androidar.MoverioLibraries;

/**
 * Created by Michael on 1/27/2016.
 *
 * Some stuff to help us debug with phones when the Moverio is with someone else
 *
 */
public final class PhoneDebug {

    private PhoneDebug() {}

    // false if using the Moverio, true otherwise
    // when set to true, tilt your phone 90 degrees counterclockwise so the left side points down.
    // this way you can view the OpenGL scene in landscape mode
    public static final boolean USING_PHONE = true;

}
