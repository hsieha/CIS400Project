package com.example.livelyturtle.androidar.activities;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.widget.TextView;

//for testing
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.Calendar;
import android.content.Context;
import java.io.OutputStream;

import com.example.livelyturtle.androidar.MoverioLibraries.PhoneDebug;
import com.example.livelyturtle.androidar.R;

public class OrientationDashboardActivity extends Activity implements
        SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_dashboard);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    float[] mGravity;
    float[] mGeomagnetic;
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float REMAP[] = new float[9];
                float orientation[] = new float[3];

                // remap for "facing forward" orientation
                // without this, we would obtain APR values for DEFAULT ORIENTATION (facing ground)
                if (PhoneDebug.USING_PHONE) {
                    SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, REMAP);
                }
                else {
                    SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, REMAP);
                }

                SensorManager.getOrientation(REMAP, orientation);
                writeAPRRadToDegrees(orientation[0], orientation[1], orientation[2]);
            }
        }
    }

    private void writeAPRRadToDegrees(float a, float p, float r) {
        int azimuth = (int) (a * 180 / (float) Math.PI);
        int pitch = (int) (p * 180 / (float) Math.PI);
        int roll = (int) (r * 180 / (float) Math.PI);
        ((TextView) findViewById(R.id.AzimuthValue)).setText(Integer.valueOf(azimuth).toString());
        ((TextView) findViewById(R.id.PitchValue)).setText(Integer.valueOf(pitch).toString());
        ((TextView) findViewById(R.id.RollValue)).setText(Integer.valueOf(roll).toString());
        writeAPRDegreesToStatus(azimuth, pitch, roll);
    }

    private void writeAPRDegreesToStatus(int a, int p, int r) {
        String azimuthStatus = "";
        String pitchStatus = "LEVEL";
        String rollStatus = "LEVEL";

        // adjustments to make the following switch easier
        if (a < 0) a += 360;
        a += 22;

        switch (a/45) {
            case 0:
            case 8:
                azimuthStatus = "N";
                break;
            case 1:
                azimuthStatus = "NE";
                break;
            case 2:
                azimuthStatus = "E";
                break;
            case 3:
                azimuthStatus = "SE";
                break;
            case 4:
                azimuthStatus = "S";
                break;
            case 5:
                azimuthStatus = "SW";
                break;
            case 6:
                azimuthStatus = "W";
                break;
            case 7:
                azimuthStatus = "NW";
                break;
            default:
                azimuthStatus = "!ERROR!";
                break;
        }

        if (p > 20) pitchStatus = "WAY DOWN";
        else if (p < -20) pitchStatus = "WAY UP";
        else if (p > 5) pitchStatus = "DOWN";
        else if (p < -5) pitchStatus = "UP";

        if (r > 20) rollStatus = "TILT WAY RIGHT";
        else if (r < -20) rollStatus = "TILT WAY LEFT";
        else if (r > 5) rollStatus = "TILT RIGHT";
        else if (r < -5) rollStatus = "TILT LEFT";


        ((TextView) findViewById(R.id.AzimuthStatus)).setText(azimuthStatus);
        ((TextView) findViewById(R.id.PitchStatus)).setText(pitchStatus);
        ((TextView) findViewById(R.id.RollStatus)).setText(rollStatus);
    }
}
