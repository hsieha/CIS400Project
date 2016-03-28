package com.example.livelyturtle.androidar.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import com.example.livelyturtle.androidar.Coordinate;
import com.example.livelyturtle.androidar.MapData;
import com.example.livelyturtle.androidar.MoverioLibraries.DataDebug;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.PhoneDebug;
import com.example.livelyturtle.androidar.Street;
import com.example.livelyturtle.androidar.opengl.MyGLRenderer;
import com.example.livelyturtle.androidar.MoverioLibraries.DataDebug.*;
import com.example.livelyturtle.androidar.Tour;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/*
 * This Activity mostly handles sensor calculations.
 * Drawing implementation can be found in MyGLRenderer.
 *
 * Sensor fusion code found online at codeproject.com/Articles/729759/Android-Sensor-Fusion-Tutorial
 * and licensed with the MIT License.
 */
public class World3DActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private Sensor mGyroscope;

    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[] {1,0,0,0,1,0,0,0,1};

    // orientation angles from accel and magnet, remapped to front-view
    private float[] accMagOrientation;

    // orientation angles calculated from the gyroscope, remapped to front-view
    private float[] gyroOrientation = new float[3];

    // the final result of fusion sensor data, remapped to front view
    private float[] APR = new float[3];


    //private LocationManager locationManager;


    private MyGLSurfaceView mGLView;

    private MapData mapData;

    private Tour tour;

    private void updateAPR(float a, float p, float r) {
        APR[0] = a;
        APR[1] = p;
        APR[2] = r;

        // The GL class also needs APR values
        mGLView.mRenderer.updateAPR(APR);
    }

    class MyGLSurfaceView extends GLSurfaceView {
        private final MyGLRenderer mRenderer;
//        public MyGLSurfaceView(Context context){
//            super(context);
//            // Create an OpenGL ES 2.0 context
//            setEGLContextClientVersion(2);
//            mRenderer = new MyGLRenderer(context);
//            // Set the Renderer for drawing on the GLSurfaceView
//            setRenderer(mRenderer);
//        }

        public MyGLSurfaceView(Context context, MapData mapData){
            super(context);
            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);
            mRenderer = new MyGLRenderer(context, mapData);
            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(mRenderer);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // fullscreen as in the bt200 technical info pdf - REMOVE BOTTOM BAR
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
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mapData = new MapData("UPennCampus.kml", this);
        mGLView = new MyGLSurfaceView(this, mapData);

        //initialize the Tour
        tour = new Tour();

        //mGLView.mRenderer.addMapData(mapData);
        setContentView(mGLView);

        // set off fusion sensor calculations at fixed intervals
        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(), 2500, TIME_CONSTANT);


        // -----LOCATION DATA-----
        // Acquire a reference to the system Location Manager
//        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//
//        // Define a listener that responds to location updates
//        LocationListener locationListener = new LocationListener() {
//            public void onLocationChanged(Location location) {
//                // Called when a new location is found by the network location provider.
//                if(!DataDebug.HARDCODE_LOCATION) {
//                    mGLView.mRenderer.updateEye(location);
//                }
//            }
//
//            public void onStatusChanged(String provider, int status, Bundle extras) {}
//
//            public void onProviderEnabled(String provider) {}
//
//            public void onProviderDisabled(String provider) {}
//        };
//
//        // Register the listener with the Location Manager to receive location updates
//        try {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, locationListener);
//        }
//        catch (SecurityException e) {
//            e.printStackTrace();
//        }

        // run the bluetooth listener, only for real location
        if (DataDebug.LOCATION_MODE == LocationMode.REAL) {
            (new AcceptThread()).start();
        }

        // FOR DEBUG ONLY...
        // wait 5s, then call renderPathTask
        renderPath(new Coordinate(39.95524,-75.2022));
        (new Timer()).schedule(new RenderPathTask(), 10000);
    }
    class RenderPathTask extends TimerTask {
        @Override
        public void run() {
            renderPath(new Coordinate(39.95247,-75.19053));
            System.out.println("renderPath called");
        }
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    float[] mGravity;
    float[] mGeomagnetic;
    float[] mGyro = new float[] {0,0,0};
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
            calculateAccMagOrientation();
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
            calculateAccMagOrientation();
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mGyro = event.values;
            processGyroData(event);
        }

//        // DEBUG DYNAMIC DRAWING - remove soon
//        if (Moverio3D.getDirectionFromAzimuth(APR[0]) == Moverio3D.CardinalDirection.SOUTHWEST) {
//            System.out.println("*** DRAWING JUNK");
//
//            // add a large green square
//            List<Moverio3D.Vector> vlist = new LinkedList<>();
//            Moverio3D.Vector BL = Moverio3D.Vector.of(-400, 0, -50);
//            Moverio3D.Vector TL = Moverio3D.Vector.of(-400, 30, -50);
//            Moverio3D.Vector TR = Moverio3D.Vector.of(-400, 30, -20);
//            Moverio3D.Vector BR = Moverio3D.Vector.of(-400, 0, -20);
//            vlist.add(BL);vlist.add(TL);vlist.add(TR);vlist.add(BR);
//            List<Short> order = new LinkedList<>();
//            order.add((short)0);order.add((short)1);order.add((short)2);
//            order.add((short)0);order.add((short)2);order.add((short)3);
//            mGLView.mRenderer.addDrawing("hello1", vlist, order, MyGLRenderer.PURE_GREEN, 1f);
//            //mGLView.mRenderer.doJunk();
//
//
//            // add light blue text into the square, id "hello2"
//            mGLView.mRenderer.addText("hello2", "TEST TEST TEST", Moverio3D.Vector.of(-390, 10, -35), MyGLRenderer.LIGHT_BLUE, 1f);
//        }
//        else {
//            System.out.println("*** REMOVING JUNK");
//            mGLView.mRenderer.removeDrawing("hello1");
//            mGLView.mRenderer.removeText("hello2");
//        }
//        // END DEBUG DYNAMIC DRAWING - REMOVE SOON

    }

    public void calculateAccMagOrientation() {
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float REMAP[] = new float[9];

                // remap for "facing forward" orientation
                // without this, we would obtain APR values for DEFAULT ORIENTATION (facing ground)
                if (PhoneDebug.USING_PHONE) {
                    SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, REMAP);
                }
                else {
                    SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, REMAP);
                }

                accMagOrientation = new float[3];
                // update storage for acc/mag orientation values
                SensorManager.getOrientation(REMAP, accMagOrientation);
            }
        }
    }

    static final float EPSILON = 0.000000001f;
    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor)
    {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                                 gyroValues[1] * gyroValues[1] +
                                 gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);

        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    static final float NS2S = 1.0f / 1000000000.0f;
    float timestamp;
    boolean initState = true;
    public void processGyroData(SensorEvent event) {
        if (accMagOrientation == null) return;

        // initialization of the gyroscope based rotation matrix
        if(initState) {
            float[] initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix); // the same as initMatrix.copy()...
            initState = false;
            System.out.println("init accmagO: " + Arrays.toString(accMagOrientation));
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            getRotationVectorFromGyro(mGyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // Sadly, after hours of testing, it seems Android is incapable of correctly producing a
        // remapped deltaMatrix. It may have something to do with floats that are too small.
        // This is complete bullshit, but those hours are already gone so
        // there's not much I can do about that.
        // Final remaining idea:
        // - remap gyroMatrix back to the default orientation, then multiply by deltaMatrix, then remap
        //   the whole thing back to forward-facing
        // - It looks like it's working.

        float[] defaultGyroMatrix = new float[9];
        float[] defaultGyroMatrixWithDeltaApplied;
        float[] remappedGyroMatrix = new float[9];
        if (PhoneDebug.USING_PHONE) {
            SensorManager.remapCoordinateSystem(gyroMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_MINUS_Z, defaultGyroMatrix);
        }
        else {
            SensorManager.remapCoordinateSystem(gyroMatrix, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, defaultGyroMatrix);
        }
        defaultGyroMatrixWithDeltaApplied = matrixMultiplication(defaultGyroMatrix, deltaMatrix);
        if (PhoneDebug.USING_PHONE) {
            SensorManager.remapCoordinateSystem(defaultGyroMatrixWithDeltaApplied, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, remappedGyroMatrix);
        }
        else {
            SensorManager.remapCoordinateSystem(defaultGyroMatrixWithDeltaApplied, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedGyroMatrix);
        }

        gyroMatrix = remappedGyroMatrix;
        // update storage for gyro orientation values
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);

        //System.out.println("gyroAPR[A]: " + gyroOrientation[0]);
    }


    /*
     * Higher frame rates look smoother, but are more susceptible to all types of errors.
     *
     * Very high filter coefficients (>.998) will suffer from standard lag, and may suffer from
     * "inertial lag" (scene inches around long after user has stopped moving head) due to heavy
     * filtering of correct orientation values.
     *
     * Very low filter coefficients (<.950) will suffer from heavy jitter due to noise from the
     * compass and/or accelerometer. However, lag and inertial lag will be less apparent.
     *
     * Set the coefficient to 1 for full gyroscopic control. (suffers from drift)
     * Set the coefficient to 0 for full accelerometer/magnetometer control. (looks like an earthquake)
     *
     * It is recommended to set a higher constant for the AZIMUTH reading due to higher noise from the compass.
     */
    static final int FRAME_RATE = 50; /*CHANGE THIS AS NEEDED*/
    static final int TIME_CONSTANT = (int) (1000./FRAME_RATE); /*DO NOT MODIFY*/
    static final float FILTER_COEFFICIENT_AZIMUTH = 1;//0.999f; /*CHANGE THIS AS NEEDED*/
    static final float FILTER_COEFFICIENT = 0.996f; /*CHANGE THIS AS NEEDED*/
    private Timer fuseTimer = new Timer();
    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            // TWOPI and the ?'s are used to fix a "sharp jerk" bug that occurs at the -pi/pi border.
            // The -1 ensures that we skip the case at the zero border (slightly neg/slightly pos).
            // (For example, this bug appears when facing south. I suppose it could also happen if the
            // user is upside-down and staring backwards, but that seems to be an unlikely scenario.)
            final float TWOPI = (float) (2. * Math.PI);
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
            float oneMinusCoeffAzimuth = 1.0f - FILTER_COEFFICIENT_AZIMUTH;
            float A =
                    FILTER_COEFFICIENT_AZIMUTH * gyroOrientation[0]
                            + oneMinusCoeffAzimuth * (gyroOrientation[0]*accMagOrientation[0] < -1 ?
                                                        (gyroOrientation[0] < 0 ?
                                                            accMagOrientation[0] - TWOPI  :
                                                            accMagOrientation[0] + TWOPI) :
                                                        accMagOrientation[0]
                                                     );
            float P =
                    FILTER_COEFFICIENT * gyroOrientation[1]
                            + oneMinusCoeff * (gyroOrientation[1]*accMagOrientation[1] < -1 ?
                                                  (gyroOrientation[1] < 0 ?
                                                      accMagOrientation[1] - TWOPI  :
                                                      accMagOrientation[1] + TWOPI) :
                                                  accMagOrientation[1]
                                              );
            float R =
                    FILTER_COEFFICIENT * gyroOrientation[2]
                            + oneMinusCoeff * (gyroOrientation[2]*accMagOrientation[2] < -1 ?
                                                  (gyroOrientation[2] < 0 ?
                                                      accMagOrientation[2] - TWOPI  :
                                                      accMagOrientation[2] + TWOPI) :
                                                  accMagOrientation[2]
                                              );
            updateAPR(A, P, R);

            //System.out.println("ACCMAG: " + Arrays.toString(accMagOrientation));
            //System.out.println("GYRO: " + Arrays.toString(gyroOrientation));

            // overwrite gyro matrix and orientation with fused orientation to compensate for gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(APR);
            System.arraycopy(APR, 0, gyroOrientation, 0, 3);
        }
    }

    // this takes a remapped orientation and gives back a remapped matrix
    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        //o[0] is azimuth, o[1] is pitch, o[2] is roll
        float sinX,cosX,sinY,cosY,sinZ,cosZ;
        sinX = (float) Math.sin(o[1]);
        cosX = (float) Math.cos(o[1]);
        sinY = (float) Math.sin(o[2]);
        cosY = (float) Math.cos(o[2]);
        sinZ = (float) Math.sin(o[0]);
        cosZ = (float) Math.cos(o[0]);

        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        return matrixMultiplication(zM, matrixMultiplication(xM, yM));
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    /* -----ANTI-JITTER-----
     * Jitter is caused by sensor noise in APR values. The compass (azimuth) is the worst offender,
     * with a total range of around 5 degrees, but all values suffer from some noise.
     *
     * (THIS IS A SOLVED PROBLEM... SEE http://plaw.info/2012/03/android-sensor-fusion-tutorial/ or
     *  codeproject.com/Articles/729759/Android-Sensor-Fusion-Tutorial)
     *
     * (The below comment is now obsolete, but it provides good insight into the process. Also, I
     *  wrote it, so I want to keep it. :D)
     *
     * =====OBSOLETE=====
     * This is a custom algorithm. The general approach is as follows:
     *  - send sensor's APR values through a filter, and discard unwanted results
     *  - store the APR that previously made it past the filter in a variable
     *    - this ensures the drawn scene stays completely still if the user is still
     *    - this gives the algorithm a "previous state" to analyze
     *  - also store all of the previous 3-5 inputs in a queue, whether they pass or not
     *  - update the APR if at any point, all queue elements show movement in the same direction
     *    - because jitter can hide slow movement, a queue of moving averages will also be used
     *      to determine if the drawn scene should update. This average will be compared with lastPassed.
     *      There may even be a queue of second-order averages (averages of the average queue).
     *    - When fast movement is detected, the averages queues should be flushed to avoid lag.
     *    - advanced: if the user is curving their view, the direction may be changing. So, it is
     *      better to take the *derivative* of the previous inputs in some fashion.
     *    - advanced: remove perpendicular jitter by determining what the direction is and
     *      smoothing out values along the perpendicular axis
     *
     * The key assumption here is that random jitter values move in all directions when the user is
     * still, making it unlikely for a queue to show directional movement.
     *
     * This algorithm satisfies several desirable characteristics:
     *  - The drawn scene is completely still if the user is not moving their head
     *  - The display will always match the true APR. (As long as the sensors are functioning, no
     *    re-calibration is needed. This algorithm is not based off of the Euler method / differences
     *    between successive inputs.)
     *  - The display will continue to be drawn correctly whether the user is moving fast or slow.
     *  - The display remains responsive. Benchmarking shows that sensor updates occur around 50x/second.
     *    The algorithm should detect whether movement is necessary within a couple hundred milliseconds.
     *
     *
    float[] lastPassed;
    private boolean passFilter(float[] in) {
        if (lastPassed == null) {
            lastPassed = in;
            return true;
        }
        float lpA = lastPassed[0];
        float lpP = lastPassed[1];
        float lpR = lastPassed[2];
        float inA = in[0];
        float inP = in[1];
        float inR = in[2];

        if (Math.abs(inA-lpA) < .125f && Math.abs(inP-lpP) < .125f && Math.abs(inR-lpR) < .125f) {
            return false;
        }
        lastPassed = in;
        return true;
    }
     *
     * =====OBSOLETE=====
     *
     *
     */


    // bluetooth stuff
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("moverio", UUID.fromString("cb34d9bc-9523-4846-bfac-ac47730eecfe"));
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            System.out.println("***AcceptThread running");
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    if (mmServerSocket != null) {
                        System.out.println("***AcceptThread trying to obtain socket...");
                        socket = mmServerSocket.accept();
                        System.out.println("***AcceptThread obtained socket");
                    }
                    else {
                        System.out.println("*** mmServerSocket is null...");
                    }
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) { }
                    break;
                }
            }
            System.out.println("***AcceptThread finished run()");
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    void manageConnectedSocket(BluetoothSocket socket) {
        // this needs to run in its own THREAD
        System.out.println("***World3DActivity manage socket");

        // set off timer to receive GPS data. Since the timertask has a while(true), this only
        // happens once.
        timer.schedule(new receiveGPSDataTask(socket), 0L);

    }
    private Timer timer = new Timer();
    private class receiveGPSDataTask extends TimerTask {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public receiveGPSDataTask(BluetoothSocket s) {
            mmSocket = s;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = s.getInputStream();
                tmpOut = s.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[4096];  // buffer store for the stream
            int bytes; // bytes returned from read()

            while (true) {
                try {
                    System.out.println("*** Attempting to read from InputStream...");
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    System.out.println("*** InputStream read has occurred!");

                    // obtain the last 16 bytes from buffer
                    if (buffer.length < 16) {
                        continue;
                    }
                    //System.out.println("***BUFFER HAS DATA");
                    byte[] d2Bytes = Arrays.copyOfRange(buffer, bytes - 8, bytes);
                    byte[] d1Bytes = Arrays.copyOfRange(buffer, bytes - 16, bytes - 8);
                    // convert into two doubles
                    System.out.println("***");
                    double d1 = Home.toDouble(d1Bytes);
                    double d2 = Home.toDouble(d2Bytes);

                    //System.out.println("*** UPDATING EYE WITH DATA RIGHT NOW");
                    mGLView.mRenderer.updateEye(d1, d2);
                    // allow a UI change while in this thread
                    //runOnUiThread(new UIVariableChangeRunnable(d1, d2));

                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    public void renderPath(Coordinate end) {
        mGLView.mRenderer.renderPath(end);
    }

//    private class UIVariableChangeRunnable implements Runnable {
//        private final double D1;
//        private final double D2;
//        public UIVariableChangeRunnable(double d1, double d2) {
//            D1 = d1;
//            D2 = d2;
//        }
//
//        @Override
//        public void run() {
//            // call updateEye
//            mGLView.mRenderer.updateEye(D1, D2);
//        }
//    }

}
