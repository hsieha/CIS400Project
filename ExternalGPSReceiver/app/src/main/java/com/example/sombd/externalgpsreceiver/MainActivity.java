package com.example.sombd.externalgpsreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // period in ms of attempted updates to the other device (Moverio)
    private final long UPDATE_PERIOD = 1000L;

    private LocationManager locationManager;

    private TextView latitudeText;
    private TextView longitudeText;
    private TextView statusText;

    private boolean locationUpdatesAvailable = false;

    public WifiP2pDevice moverioDevice;

    public void setMoverioDevice(WifiP2pDevice m) {
        moverioDevice = m;
        state = ConnectionState.MOVERIO_FOUND;
    }

    // attempt to advance state or send data at regular intervals
    public enum ConnectionState {
        SEARCHING,
        MOVERIO_FOUND,
        CONNECTED
    }
    public void setConnectionState (ConnectionState s) {
        state = s;
    }
    private ConnectionState state;
    private Timer timer = new Timer();

    class MainAction extends TimerTask {
        public void run() {

            if (state == null) return;

            // if searching, try to find
            if (state == ConnectionState.SEARCHING) {
                if (mManager == null || mChannel == null) return;
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        setMessage("Wifi Direct discovery initialized...");
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                    }
                });
            }

            // if found, try to connect
            else if (state == ConnectionState.MOVERIO_FOUND) {
                // this means moverioDevice is properly set

                // see http://stackoverflow.com/questions/5161951
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setMessage("Moverio has been detected");
                    }
                });

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = moverioDevice.deviceAddress;
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        state = ConnectionState.CONNECTED;
                    }

                    @Override
                    public void onFailure(int reason) {
                    }
                });
            }

            // if connected, AND GPS IS WORKING, push location update
            else if (state == ConnectionState.CONNECTED) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSuccessMessage("Moverio is connected!");
                    }
                });

                if (locationUpdatesAvailable) {
                    Location l;

                    try {
                        l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        // communicate change to peer device (i.e. Moverio)

                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    void pushLocationUpdate() {
        this.getApplicationContext();
    }

    // Wi-Fi Direct apparatus
    WifiP2pManager mManager;
    Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    public void setErrorMessage(String message) {
        statusText.setText(message);
        statusText.setTextColor(getResources().getColor(R.color.red));
    }
    public void setSuccessMessage(String message) {
        statusText.setText(message);
        statusText.setTextColor(getResources().getColor(R.color.green));
    }
    public void setMessage(String message) {
        statusText.setText(message);
        statusText.setTextColor(getResources().getColor(R.color.black));
    }

    private void updateLocalDashboard() {
        Location l;
        try {
            l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            latitudeText.setText(Double.valueOf(l.getLatitude()).toString());
            longitudeText.setText(Double.valueOf(l.getLongitude()).toString());
            locationUpdatesAvailable = true;
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
        statusText = (TextView) findViewById(R.id.statusText);

        state = ConnectionState.SEARCHING;

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {

            // Called when a new location is found by the network location provider.
            public void onLocationChanged(Location location) {
                updateLocalDashboard();
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0F, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        timer.scheduleAtFixedRate(new MainAction(), 0L, UPDATE_PERIOD);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register the broadcast receiver with the intent values to be matched
        registerReceiver(mReceiver, mIntentFilter);
        (timer = new Timer()).scheduleAtFixedRate(new MainAction(), 0L, UPDATE_PERIOD);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister the broadcast receiver
        unregisterReceiver(mReceiver);
        timer.cancel();
    }
}
