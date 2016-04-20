package com.example.livelyturtle.androidar.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.livelyturtle.androidar.MapData;
import com.example.livelyturtle.androidar.MoverioLibraries.DataDebug;
import com.example.livelyturtle.androidar.SensorFusion.orientationProvider.ImprovedOrientationSensor1Provider;
import com.example.livelyturtle.androidar.SensorFusion.orientationProvider.OrientationProvider;
import com.example.livelyturtle.androidar.opengl.MyGLRenderer;
import com.example.livelyturtle.androidar.MoverioLibraries.DataDebug.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/*
 * This Activity mostly handles sensor calculations.
 * Drawing implementation can be found in MyGLRenderer.
 */
public class World3DActivity extends Activity {

    private static final int FRAME_RATE = 60; // Max FPS (this controls sensor updates only, NOT how
                                              // efficient OpenGL rendering is!)

    // the final result of fusion sensor data, remapped to front view
    private float[] APR = new float[3];

    //The current orientation provider that delivers device orientation.
    private OrientationProvider currentOrientationProvider;

    private MyGLSurfaceView mGLView;

    private MapData mapData;

    private void updateAPR(float a, float p, float r) {
        APR[0] = a;
        APR[1] = p;
        APR[2] = r;

        // The GL class also needs APR values
        mGLView.mRenderer.updateAPR(APR);
    }

    class MyGLSurfaceView extends GLSurfaceView {
        private final MyGLRenderer mRenderer;

        public MyGLSurfaceView(Context context, MapData mapData){
            super(context);
            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);
            mRenderer = new MyGLRenderer(context, mapData);
            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(mRenderer);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    System.out.println("TOUCH!!");
                    mRenderer.recenterCompass();
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        currentOrientationProvider = new ImprovedOrientationSensor1Provider(
                (SensorManager)getSystemService(SENSOR_SERVICE));

        mapData = new MapData("UPennCampus.kml", this);

        // Create a GLSurfaceView instance and set it as the ContentView for this Activity.
        mGLView = new MyGLSurfaceView(this, mapData);
        setContentView(mGLView);

        // set off APR updates at a fixed rate
        orientationTimer.scheduleAtFixedRate(new orientationProviderUpdateTask(), 0, (long) (1000. / FRAME_RATE));

        LinearLayout ll = new LinearLayout(this);
        Button b = new Button(this);
        b.setText("Start Tour!");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGLView.mRenderer.initializeTour();
                v.setVisibility(View.GONE);
            }
        });
        ll.addView(b);
        ll.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        this.addContentView(ll,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // run the bluetooth listener, only for real location
        if (DataDebug.LOCATION_MODE == LocationMode.REAL) {
            (new AcceptThread()).start();
        }
    }
    private Timer orientationTimer = new Timer();
    class orientationProviderUpdateTask extends TimerTask {
        @Override
        public void run() {
            float[] angles = currentOrientationProvider.getEulerAnglesArray();
            updateAPR(angles[0], angles[1], angles[2]);
        }
    }

    protected void onResume() {
        super.onResume();

        currentOrientationProvider.start();
        mGLView.onResume();
    }

    protected void onPause() {
        super.onPause();

        currentOrientationProvider.stop();
        mGLView.onPause();
    }

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

}
