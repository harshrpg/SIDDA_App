package com.example.harsh.intheflow;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.iid.InstanceID;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class SensorMonitorService extends Service {
    //------------------------------------------------
    // Instance Variables
    //------------------------------------------------
    private static final String TAG = SensorMonitorService.class.getSimpleName();
    private LocationManager mLocationManager = null; // Location services is mananged by this
    private SensorManager mSensorManager = null; // All other sensors e.g. Accelerometer, Gyroscope
    private Sensor lAccel, gyro; // Sensors that we will need
    private SensorEventListener sensorListener; // Listener for the sensors listed
    // above
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private static final int LOCATION_INTERVAL = 30000;
    private static final float LOCATION_DISTANCE = 10f;
    private final AsyncHttpClient client = new AsyncHttpClient();
    private final RequestParams params = new RequestParams();
    public static final String ACTION_SENSOR_BROADCAST = SensorMonitorService.class.getName() +
            "SensorBroadcast";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ACCELEROMETER_X = "acc_x";
    public static final String ACCELEROMETER_Y = "acc_y";
    public static final String ACCELEROMETER_Z = "acc_z";
    public static final String GYROSCOPE_X = "gyro_x";
    public static final String GYROSCOPE_Y = "gyro_y";
    public static final String GYROSCOPE_Z = "gyro_z";
    public String locations;
    public double[] location_b = new double[2];
    public float[] acceleration_values = new float[3];
    public float[] gyroscope_values = new float[3];
    // Unique device id generated here

    public static String iid;

    private long lastSuccessfulCallTime = 0;
    //------------------------------------------------
    // Instance Variables
    //------------------------------------------------

    //------------------------------------------------
    // Constructor
    //------------------------------------------------
    public SensorMonitorService() {
    }
    //------------------------------------------------
    // Constructor
    //------------------------------------------------

    /**
     * Location Listener inner class to get locations in every 30 seconds
     */
    private class LocationListener implements android.location.LocationListener{
        // Instance Variables
        Location mLastLocation;

        public LocationListener(String provider){
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            // If we need to send a message from a service to UI we need to broadcast it in some way
            locations = String.valueOf(location.getLatitude())+","+String.valueOf(location
                    .getLongitude());
            location_b[0] = location.getLatitude();
            location_b[1] = location.getLongitude();
            try {
                sendToServer(locations,"Location");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    // A class to listen to accelerometer and gyroscope
    private class SensorListener implements SensorEventListener{
        Sensor acceleration;

        // We will use the default constructor so no need to implement it

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Get the values everytime the accuracy is changed
            Log.d(TAG, sensor.toString()+Integer.toString(accuracy));
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // Get the values everytime the sensor data changes
            Log.d(TAG,Integer.toString(event.values.length));
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                try {
                    getAccelerometerData(event);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
                try {
                    getGyroscopeData(event);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Create the objects for the location listener to get the location if both GPS and Internet
    // is available
    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    // Method that receives and broadcasts the accelerometer data
    private void getAccelerometerData(SensorEvent event) throws InterruptedException {
        float[] values = event.values;
        String sValues = String.valueOf(event.values[0])+","+String.valueOf(event
                .values[1])+","+String.valueOf(event.values[2]);
        acceleration_values = values; // Setting the global variable with acceleration data to
        // perform a final check before sending the data to the server. This code is temporary
        // and will be replaced with a much robust solution
        if (locations != ""){
            sendToServer(sValues,"Accelerometer");
        }

    }
    //
    // Method that receives and broadcasts the gyroscope data
    private void getGyroscopeData(SensorEvent event) throws InterruptedException {
        float[] values = event.values;
        String sValues = String.valueOf(event.values[0])+","+String.valueOf(event
                .values[1])+","+String.valueOf(event.values[2]);
        gyroscope_values = values; // Refer the acceleration description
        if (locations != ""){
            sendToServer(sValues,"Gyroscope");
        }
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        // Get the accelerometer sensor here
        sensorListener = new SensorListener();
        if(mSensorManager != null){
            lAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // Get the
            // accelerometer
            gyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            Boolean isAccelSupported = mSensorManager.registerListener(sensorListener,lAccel,
                    SensorManager
                            .SENSOR_DELAY_NORMAL);// Register the listener for the accelerometer
            Boolean isGyroSupported = mSensorManager.registerListener(sensorListener,gyro,
                    SensorManager
                            .SENSOR_DELAY_NORMAL);
            Log.e(TAG, "Is Gyroscope"+Boolean.toString(isGyroSupported));
            Log.e(TAG, "Is Accel"+Boolean.toString(isAccelSupported));
        }
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        iid = String.valueOf(InstanceID.getInstance(getApplicationContext()));
        initializeSensorManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        if (location_b!=null && acceleration_values !=null && gyroscope_values != null){

            broadcastDataToServer();

        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                } finally {
                    mLocationManager = null;
                }
            }
        }
        if (mSensorManager != null) {
            try{
                mSensorManager.unregisterListener(sensorListener,lAccel);
                mSensorManager.unregisterListener(sensorListener,gyro);
            } catch (Exception ex){
                Log.i(TAG, "failed to unregister listeners");
            } finally {
                mSensorManager = null;
            }
        }
        if (mLocationManager == null && mSensorManager == null){
            Toast.makeText(this, "Data Sharing is stopped",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Something went wrong",Toast.LENGTH_LONG).show();
        }
        stopSelf();

    }
    private void initializeSensorManager() {
        Log.e(TAG, "initializeSensorManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        if (mSensorManager == null){
            mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context
                    .SENSOR_SERVICE);
        }
    }

    private void sendToServer(String values, String flag) throws InterruptedException{
        // Check if all the global variables are set
        if (location_b!=null && acceleration_values !=null && gyroscope_values != null){

            broadcastDataToServer();

        }
    }

    private void broadcastDataToServer(){
        long currentCallTime = System.currentTimeMillis();
        if (currentCallTime > lastSuccessfulCallTime + 250L) {
            lastSuccessfulCallTime = currentCallTime;
        } else return;
        params.put("device_id",iid);
        params.put("latitude",location_b[0]);
        params.put("longitude",location_b[1]);
        params.put("accelerometer_x",acceleration_values[0]);
        params.put("accelerometer_y",acceleration_values[1]);
        params.put("accelerometer_z",acceleration_values[2]);
        params.put("gyroscope_x",gyroscope_values[0]);
        params.put("gyroscope_y",gyroscope_values[1]);
        params.put("gyroscope_z",gyroscope_values[2]);
        params.put("timestamp",currentCallTime);
        Log.i("Post ",String.valueOf(params));
        client.get("https://ssida.herokuapp.com/rawdata",params, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 201) Toast.makeText(getApplicationContext(),"Success" , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
                Log.i("Response",String.valueOf(error));
            }
            // ----New Overridden method
            @Override
            public boolean getUseSynchronousMode() {
                return false;
            }

        });



    }
}
