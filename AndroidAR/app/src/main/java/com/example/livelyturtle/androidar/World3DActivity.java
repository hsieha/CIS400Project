package com.example.livelyturtle.androidar;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.livelyturtle.androidar.MoverioLibraries.PhoneDebug;

public class World3DActivity extends Activity implements
        SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;

    private float[] upVector = new float[3];
    private float[] APR = new float[3];

    private MyGLSurfaceView mGLView;

    {
        upVector[0] = 0f;
        upVector[1] = 1f;
        upVector[2] = 0f;
        APR[0] = 0f;
        APR[1] = 0f;
        APR[2] = 0f;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("***CREATION IS HAPPENING FOR WORLD 3D ACTIVITY :o :o :o");

        // fullscreen as in the bt200 technical info pdf - REMOVE BOTTOM BAR
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        // FLAG_SMARTFULLSCREEN is 0x80_00_00_00
        // winParams.flags |= WindowManager.LayoutParams.FLAG_SMARTFULLSCREEN;
        winParams.flags |= 0x80000000;
        win.setAttributes(winParams);

        // stackoverflow answer - REMOVE TOP BAR
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
    }

    class MyGLSurfaceView extends GLSurfaceView {

        private final MyGLRenderer mRenderer;

        public MyGLSurfaceView(Context context){
            super(context);

            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);

            mRenderer = new MyGLRenderer(context);

            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(mRenderer);
        }
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //mGravity = event.values;
            mGravity = lowPass(event.values.clone(), mGravity);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            //mGeomagnetic = event.values;
            mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
        }
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
                updateAPR(orientation[0], orientation[1], orientation[2]);
            }
        }
    }

    private void updateAPR(float a, float p, float r) {
        APR[0] = a;
        APR[1] = p;
        APR[2] = r;
        mGLView.mRenderer.updateAPR(APR);
    }

    // Low-pass filtering to reduce jitter
    // Source: built.io/blog/2013/05/applying-low-pass-filter-to-android-sensors-readings/
    static final float ALPHA = .325f;
    private float[] lowPass(float[] input, float output[]) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
