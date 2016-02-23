package com.example.livelyturtle.androidar.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.texample2.Texample2;
import com.example.livelyturtle.androidar.MapData;
import com.example.livelyturtle.androidar.R;
import com.example.livelyturtle.androidar.WorldObject;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;

import com.example.livelyturtle.androidar.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Home extends Activity implements
        ConnectionCallbacks, OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mBTText;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mLatitudeText = (TextView) findViewById(R.id.mLatitudeText);
        mLongitudeText = (TextView) findViewById(R.id.mLongitudeText);
        mBTText = (TextView) findViewById(R.id.BTYesNo);

//        //OPENGLIFYING WORLD_OBJECTS TEST
//        //HOW DO YOU PRINT OUT MOVERIO.VECTORS?
//        //VECTOR ORDER WORKS OUT! COO
//
//        //part of locust from rodin to the tampons statue
//        ArrayList<Coordinate> street_coor = new ArrayList<Coordinate>();
//        street_coor.add(new Coordinate(39.952774, -75.201233));
//        street_coor.add(new Coordinate(39.952694, -75200521));
//
//        //Harrison front rectangle of Harrison
//        ArrayList<Coordinate> building_coor = new ArrayList<Coordinate>();
//        building_coor.add(new Coordinate(39.952125, -75.200935));
//        building_coor.add(new Coordinate(39.9521904, -75.200983));
//        building_coor.add(new Coordinate(39.9521935, -75.201207));
//        building_coor.add(new Coordinate(39.952200, -75.201141));
//
//        Street test_street = new Street("locust_piece", street_coor);
//        Building test_building = new Building("harrison_front", building_coor);
//
//        System.out.println("Street vectors" + test_street.vectors());
//        System.out.println("Street vector order" + test_street.vector_order().toString());
//        System.out.println("Building vectors" + test_building.vectors());
//        System.out.println("Building vector order" + test_building.vector_order().toString());
//
//        //OPENGLIFYING WORLD_OBJECTS TESTING ENDS HERE


        if (false) {
            // mGoogleApiClient (gplay needed)
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            } else {
                System.out.println("***mGoogleApiClient wasn't even null to begin with!");
            }
            setContentView(R.layout.activity_home);

            mRequestingLocationUpdates = true;
            createLocationRequest();
        }
        //else {
        if (false) {
            // older android.location GPS functionality
            // Acquire a reference to the system Location Manager
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            // Define a listener that responds to location updates
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    writeLatLongToScreen();
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                    writeLatLongToScreen();
                }

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, locationListener);

            writeLatLongToScreen();
        }

        (new AcceptThread()).start();
    }

    protected void writeLatLongToScreen() {
        Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (l != null) {
            mLatitudeText.setText(Double.valueOf(l.getLatitude()).toString());
            mLongitudeText.setText(Double.valueOf(l.getLongitude()).toString());
        }
        else {
            System.out.println("***DIDN'T WRITE NEW LAT/LONG VALUES...");
        }
    }

    // anything data that comes from the phone can be thrown into the mix as well
    void writeExternalLatLongToScreen(double la, double lo) {
        mLatitudeText.setText(Double.valueOf(la).toString());
        mLongitudeText.setText(Double.valueOf(lo).toString());
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void onStart() {
        if (mGoogleApiClient != null) mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            //stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            //startLocationUpdates();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = (mGoogleApiClient != null) ? LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient) : null;
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            System.out.println("LAT WRITTEN AS: " + String.valueOf(mLastLocation.getLatitude()));
            System.out.println("LONG WRITTEN AS: " + String.valueOf(mLastLocation.getLongitude()));
        }
        else {
            mLatitudeText.setText("NULL");
            mLongitudeText.setText("NULL");
        }

        if (mRequestingLocationUpdates) {
            //startLocationUpdates();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        //
        // More about this in the 'Handle Connection Failures' section.
    }
//
//    protected void startLocationUpdates() {
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                mGoogleApiClient, mLocationRequest, this);
//    }
//
//    protected void stopLocationUpdates() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(
//                mGoogleApiClient, this);
//    }

    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateUI();
    }

    private void updateUI() {
        mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
        mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
    }


    // ----- BUTTONS ON HOME SCREEN -----

    public void advanceFromHome(View view) {
        Intent intent = new Intent(this, FullScreenActivity.class);
        startActivity(intent);
    }

    public void toCameraActivity(View view) {
        Intent intent = new Intent(this, CameraPreviewActivity.class);
        startActivity(intent);
    }

    public void toOrientationActivity(View view) {
        Intent intent = new Intent(this, OrientationDashboardActivity.class);
        startActivity(intent);
    }

    public void toWorldActivity(View view) {
        Intent intent = new Intent(this, World3DActivity.class);
        startActivity(intent);
    }

    public void toTextSampleActivity(View view) {
        Intent intent = new Intent(this, TextSampleActivity.class);
        startActivity(intent);
    }

    public void toTexample(View view) {
        Intent intent = new Intent(this, Texample2.class);
        startActivity(intent);
    }

    public void toMapsActivity(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
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
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    if (mmServerSocket != null) {
                        socket = mmServerSocket.accept();
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
        System.out.println("***manage socket");

        // allow a UI change while in this thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBTText.setText("CONNECTED");
            }
        });

        // set off timer to receive GPS data. This has a while(true) loop, so it only needs to be set off once.
        timer.schedule(new receiveGPSDataTask(socket), 0L);

    }
    private Timer timer = new Timer();
    class receiveGPSDataTask extends TimerTask {

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
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[4096];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);


                    // obtain the last 16 bytes from buffer
                    if (buffer.length < 16) { continue; }
                    System.out.println("***BUFFER HAS DATA");
                    byte[] d2Bytes = Arrays.copyOfRange(buffer, bytes-8, bytes);
                    byte[] d1Bytes = Arrays.copyOfRange(buffer, bytes-16, bytes-8);
                    // convert into two doubles
                    double d1 = toDouble(d1Bytes);
                    double d2 = toDouble(d2Bytes);

                    // allow a UI change while in this thread
                    runOnUiThread(new UIVariableChangeRunnable(d1, d2));

                } catch (IOException e) {
                    break;
                }
            }
        }
    }
    class UIVariableChangeRunnable implements Runnable {
        private final double D1;
        private final double D2;
        public UIVariableChangeRunnable(double d1, double d2) {
            D1 = d1;
            D2 = d2;
        }

        @Override
        public void run() {
            // write them
            writeExternalLatLongToScreen(D1, D2);
        }
    }

    // http://stackoverflow.com/questions/2905556
    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }


}
