package com.example.sombd.externalgpsreceiver;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;

    private TextView latitudeText;
    private TextView longitudeText;

    private void pushLocationUpdate() {

        Location l;
        try {
            l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            // update local display
            latitudeText.setText(Double.valueOf(l.getLatitude()).toString());
            longitudeText.setText(Double.valueOf(l.getLongitude()).toString());

            // communicate changes to peer devices (i.e. Moverio)

            // determine if connection is active
            // send data

        }
        catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeText = (TextView) findViewById(R.id.latitudeText);
        longitudeText = (TextView) findViewById(R.id.longitudeText);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {

            // Called when a new location is found by the network location provider.
            public void onLocationChanged(Location location) {
                pushLocationUpdate();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }
}
