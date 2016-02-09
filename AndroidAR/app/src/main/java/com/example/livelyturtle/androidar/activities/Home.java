package com.example.livelyturtle.androidar.activities;

import android.app.Activity;
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

import java.util.ArrayList;
import java.util.Arrays;

public class Home extends Activity implements
        ConnectionCallbacks, OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapData mapData = new MapData("UPennCampus.kml", this);

        setContentView(R.layout.activity_home);

        mLatitudeText = (TextView) findViewById(R.id.mLatitudeText);
        mLongitudeText = (TextView) findViewById(R.id.mLongitudeText);

        //OPENGLIFYING WORLD_OBJECTS TEST
        //HOW DO YOU PRINT OUT MOVERIO.VECTORS?
        //VECTOR ORDER WORKS OUT! COO

        //part of locust from rodin to the tampons statue
        ArrayList<Coordinate> street_coor = new ArrayList<Coordinate>();
        street_coor.add(new Coordinate(39.952774, -75.201233));
        street_coor.add(new Coordinate(39.952694, -75200521));

        //Harrison front rectangle of Harrison
        ArrayList<Coordinate> building_coor = new ArrayList<Coordinate>();
        building_coor.add(new Coordinate(39.952125, -75.200935));
        building_coor.add(new Coordinate(39.9521904, -75.200983));
        building_coor.add(new Coordinate(39.9521935, -75.201207));
        building_coor.add(new Coordinate(39.952200, -75.201141));

        Street test_street = new Street("locust_piece", street_coor);
        Building test_building = new Building("harrison_front", building_coor);

        System.out.println("Street vectors" + test_street.vectors());
        System.out.println("Street vector order" + test_street.vector_order());
        System.out.println("Building vectors" + test_building.vectors());
        System.out.println("Building vector order" + test_building.vector_order());

        //OPENGLIFYING WORLD_OBJECTS TESTING ENDS HERE


        if (false) {
            System.out.println("***ABOUT TO MAKE mGoogleApiClient");
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            } else {
                System.out.println("***mGoogleApiClient wasn't even null to begin with!");
            }
            System.out.println("***mGoogleApiClient: " + mGoogleApiClient);
            setContentView(R.layout.activity_home);

            mRequestingLocationUpdates = true;
            createLocationRequest();
        }
        //else {
        if (false) {
            System.out.println("***GoogleApiClient is not being used!");
            System.out.println("***Proceeding with android.location GPS functionality...");
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
            System.out.println("LAT: " + String.valueOf(mLastLocation.getLatitude()));
            System.out.println("LONG: " + String.valueOf(mLastLocation.getLongitude()));
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


}
