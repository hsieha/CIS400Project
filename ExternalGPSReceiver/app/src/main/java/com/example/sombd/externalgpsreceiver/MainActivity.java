package com.example.sombd.externalgpsreceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // period in ms of attempted updates to the other device (Moverio)
    private final long UPDATE_PERIOD = 1000L;

    private LocationManager locationManager;

    private TextView latitudeText;
    private TextView longitudeText;
    private TextView statusText;
    private double latDouble;
    private double longDouble;

    private boolean locationUpdatesAvailable = false;

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
            latDouble = Double.valueOf(l.getLatitude());
            longDouble = Double.valueOf(l.getLongitude());
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


        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if ("88:33:14:1F:04:1B".equals(device.getAddress())) {
                    mMoverioBTDevice = device;
                    System.out.println("***BLUETOOTH DEVICE FOUND");
                    break;
                }
            }
            if (mMoverioBTDevice == null) {
                System.out.println("***MOVERIO NOT FOUND IN BLUETOOTH PAIRED DEVICES");
            }
        }

        ConnectThread t = new ConnectThread(mMoverioBTDevice);
        t.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // bluetooth stuff
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice mMoverioBTDevice = null;

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("cb34d9bc-9523-4846-bfac-ac47730eecfe"));
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                System.out.println("***CONNECTION ATTEMPT START");
                mmSocket.connect();
                System.out.println("***CONNECTION SUCCEEDED");

                // allow a UI change while in this thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setMessage("Connected; data is not being transferred...");
                    }
                });
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private Timer timer = new Timer();
    class sendGPSDataTask extends TimerTask {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public sendGPSDataTask(BluetoothSocket s) {
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
            System.out.println("***SEND GPS DATA TASK");

            if (locationUpdatesAvailable) {

                System.out.println("*** WRITING GPS DATA NOW...");

                byte[] latBytes = toByteArray(latDouble);
                byte[] longBytes = toByteArray(longDouble);

                byte[] data = byteConcat(latBytes, longBytes);
                System.out.println("***DATA LEN: " + data.length);

                try {
                    mmOutStream.write(data);
                } catch (IOException e) { }

            }
        }
    }
    void manageConnectedSocket(BluetoothSocket socket) {
        // if GPS data is available, start writing to the socket - THREAD
        System.out.println("***MANAGING SOCKET");

        // allow a UI change while in this thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setSuccessMessage("CONNECTED");
            }
        });

        // set off timer to send GPS data
        timer.scheduleAtFixedRate(new sendGPSDataTask(socket), 0, 1000L);

    }

    // http://stackoverflow.com/questions/2905556
    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    // http://stackoverflow.com/questions/80476
    public byte[] byteConcat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

}
