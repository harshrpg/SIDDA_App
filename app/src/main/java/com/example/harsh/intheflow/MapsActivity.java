package com.example.harsh.intheflow;

//import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import cz.msebera.android.httpclient.Header;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // camera position shows the location on the map
    private CameraPosition mCameraPosition;
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private GoogleMap mMap;

    private static double LOWER_INTENSITY_LIMIT = 0;
    private static double HIGHER_INTENSITY_LIMIT = 1;
    // Entry point to the places API
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // Entry point to Fused Location Provider
    private FusedLocationProviderClient mFusedLocationProviderClient;

    public static Activity mapsActivity;

    private LinearLayout layout_2, layout_1;

    // Create a provider to build a heatmap tile
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay; // Tileoverlay takes the heatmap tile and puts it on the map

    // Defining colors for the heatmap tile
    private final int[] colors = {
            Color.rgb(0,132,255), // blue
            Color.rgb(255,252,0), // yellow
            Color.rgb(255,102,0), // orange
            Color.rgb(255, 0, 0)    // red
    };

    private final float[] startPoints = {
            0.25f, 0.50f, 0.75f, 1f
    };

    private final Gradient gradient = new Gradient(colors, startPoints);

    // markers
    private List<Marker> markerList = new ArrayList<>();


    private final LatLng mDefaultLocation = new LatLng(6.2306, 53.3498);
    private static final int DEFAULT_ZOOM = 15;
    private boolean mLocationPermissionGranted;
    // The geographical location where the device is currently located. The last known location
    // provided by the fused location provider
    private Location mLastKnownLocation;
    protected JSONArray getMapData;
    // Get the AsyncHttpClient to get the data from the API
    private final AsyncHttpClient client = new AsyncHttpClient();
    private static final String GET_URL  = "https://ssida.herokuapp.com/getmapdata";

    private static final String TAG = MapsActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state
        if (savedInstanceState != null){
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map
        setContentView(R.layout.activity_maps);

        // Construct a GeoDataClient
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // Construct a FusedLocationProvider
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Create the layout_2
        layout_2 = findViewById(R.id.layout_2);
        layout_1 = findViewById(R.id.layout_1);

        killOtherActivity();

        // Required for permission handler
        mapsActivity = this;


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Get the checkbox listener here
        final CheckBox relaxedCheckBox = findViewById(R.id.Relaxed_CheckBox);
        final CheckBox energeticCheckBox = findViewById(R.id.Energetic_CheckBox);
        final CheckBox serviceCheckBox = findViewById(R.id.service_check);

        serviceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b){
                    stopService(new Intent(getApplicationContext(), SensorMonitorService.class));
                } else {
                    startService(new Intent(getApplicationContext(), SensorMonitorService.class));
                }
            }
        });

        relaxedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    if (energeticCheckBox.isChecked()){
                        HIGHER_INTENSITY_LIMIT = 1;
                        LOWER_INTENSITY_LIMIT = 0;
                    } else {
                        HIGHER_INTENSITY_LIMIT = 0.5;
                        LOWER_INTENSITY_LIMIT = 0;
                    }
                    try {
                        getLiveData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (energeticCheckBox.isChecked()){
                        HIGHER_INTENSITY_LIMIT = 1;
                        LOWER_INTENSITY_LIMIT = 0.51;
                    } else {
                        HIGHER_INTENSITY_LIMIT = 1;
                        LOWER_INTENSITY_LIMIT = 0;
                    }
                    try {

                        getLiveData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        energeticCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    if (relaxedCheckBox.isChecked()){
                        HIGHER_INTENSITY_LIMIT = 1;
                        LOWER_INTENSITY_LIMIT = 0;
                    } else {
                        HIGHER_INTENSITY_LIMIT = 1;
                        LOWER_INTENSITY_LIMIT = 0.51;
                    }
                    try {
                        getLiveData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (relaxedCheckBox.isChecked()){
                        HIGHER_INTENSITY_LIMIT = 0.5;
                        LOWER_INTENSITY_LIMIT = 0;
                    } else {
                        HIGHER_INTENSITY_LIMIT = 1;
                        LOWER_INTENSITY_LIMIT = 0;
                    }
                    try {
                        getLiveData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mMap != null){
                    if (mLocationPermissionGranted){
                        try{
                            getLiveData();
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }

            }
        },30000);



    }

    public void openOptions(View view){
//        Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
        if (layout_2.getVisibility() == View.GONE){
            // If the layout is hidden
            layout_2.setVisibility(View.VISIBLE);
            layout_2.animate().translationY(layout_1.getHeight());
        } else {
            layout_2.animate().translationY(0);
            layout_2.setVisibility(View.GONE);
        }
    }

    private void killOtherActivity(){
        if (MainActivity.mainActivity != null && PersonnaActivity.personnaActivity !=null){
            MainActivity.mainActivity.finish();
            PersonnaActivity.personnaActivity.finish();
        }
    }

    /**
     * Saves the state of the map on activity pause
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null){
            outState.putParcelable(KEY_CAMERA_POSITION,mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION,mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }



    private void requestLocationPermission(){
        Dexter.withActivity(this).withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.areAllPermissionsGranted()){
                            mLocationPermissionGranted = true;
                            getDeviceLocation();
                            startService(new Intent(getApplicationContext(), SensorMonitorService.class));
                        }

                        if (report.isAnyPermissionPermanentlyDenied()){
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
//                        Toast.makeText(getApplicationContext(), "Error Occured in requesting " +
//                                "permissions",Toast.LENGTH_LONG).show();
                        Log.e(TAG,"Error occured in requesting location");
                    }
                })
                .onSameThread().check();
    }
    /**
     * Show the settings dialog
     */
    private void showSettingsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in " +
                "app settings");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void openSettings(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Style the map
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }


        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        // Request the permission
        requestLocationPermission();

         updateLocationUI();
         getDeviceLocation();

         // Run the marker listener here
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                Toast.makeText(getApplicationContext(),"marker is clicked",Toast.LENGTH_SHORT).show();
                return false;

            }
        });

    }

    /**
     * Gets the current location of the device and sets the camera position
     */
    private void getDeviceLocation(){
        try {
            if (mLocationPermissionGranted){
                // If all permissions are granted then
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(Task<Location> task) {
                        if (task.isSuccessful()){
                            // Set the maps camera on current location
                            mLastKnownLocation = task.getResult();
                            try{
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()),DEFAULT_ZOOM
                                ));
                            } catch(NullPointerException e){
                                e.printStackTrace();
                                Log.i("Ernestas APP",String.valueOf(e));
                                Toast.makeText(getApplicationContext(),"Please turn on your " +
                                        "Location",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            } finally {
                                // Run the getData function here
                                updateLocationUI();
                            }
                        } else {
                            Log.d("LocationResult","Current Location is null. Using defaults");
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation,
                                    DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e){
            Log.e("Exception: %s",e.getMessage());
        }
    }

    private void updateLocationUI(){
        if (mMap == null){
            return;
        }
        try{
            if (mLocationPermissionGranted){

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                try {
                    getLiveData();
                } catch (JSONException e){
                    Toast.makeText(getApplicationContext(),"JSON Exception",Toast.LENGTH_SHORT)
                            .show();
                    Log.e(TAG, "JSON Exception");
                }

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                requestLocationPermission();
            }
        } catch (SecurityException e){
            Log.e("Exception: %s",e.getMessage());
        }
    }

    /**
     * Adds a heatmap to the map
     * Currently only works with button click
     */
//    public void addHeatMap() throws InterruptedException{
//        Toast.makeText(this,"Clicked in heatmap function",Toast.LENGTH_SHORT).show();
//        List<WeightedLatLng> list = null;
//        try{
//            list = getLiveData();
//        }catch (JSONException e){
//            Log.e("Listerr","Error in reading json data for heatmap");
//        }
//        if (list != null){
//            mProvider = new HeatmapTileProvider.Builder().weightedData(list).build();
//            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
//
//        } else {
//            Toast.makeText(getApplicationContext(),"Sleeping time",Toast.LENGTH_SHORT).show();
//            TimeUnit.SECONDS.sleep(3);
//            addHeatMap();
//        }
//    }

    /**
     * Checks the api on location change and updates the data
     * @return list that contains the location with scores
     */
    public void getLiveData() throws JSONException {
        // Create a new array list
//        ArrayList<WeightedLatLng> list = new ArrayList<WeightedLatLng>();
        //Toast.makeText(this,"Live data",Toast.LENGTH_SHORT).show();
        client.get(GET_URL, new AsyncHttpResponseHandler()  {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    int drawable_icon;
                    getMapData = new JSONArray(new String(responseBody));
                    ArrayList<WeightedLatLng> list = new ArrayList<WeightedLatLng>();

                    if (getMapData!=null){
                        if (markerList!=null){
                            for (Marker m :
                                    markerList) {
                                if (m!=null) m.remove();
                            }
                        }

                        for (int i=0; i < getMapData.length(); i++){
                            JSONObject object = getMapData.getJSONObject(i);
                            JSONObject fields = object.getJSONObject("fields");
                            double lat = fields.getDouble("latitude");
                            double lng = fields.getDouble("longitude");
                            double intensity = fields.getDouble("score");
                            Log.i("Intensity_values",String.valueOf(intensity));
                            if (intensity>0.5){
                                drawable_icon = R.drawable.ic_stress_free_marker_energy_white;
                            } else {
                                drawable_icon = R.drawable.ic_stress_free_marker_white;
                            }
                            if ((intensity>=LOWER_INTENSITY_LIMIT) && (intensity <=
                                    HIGHER_INTENSITY_LIMIT)){
                                Log.i("Intensity_Readings",String.valueOf(intensity));
                                WeightedLatLng weightedLatLng = new WeightedLatLng(new LatLng(lat,lng),intensity);
                                list.add(weightedLatLng);
                                markerList.add(mMap.addMarker(new MarkerOptions().position(new 
                                        LatLng
                                        (lat,
                                        lng))
                                        .icon(bitmapDescriptorFromVector(getApplicationContext(),
                                                drawable_icon
                                                ))));
                            }

                        }
                        if (mOverlay != null){
                            mOverlay.remove();
                        }

                        mProvider = new HeatmapTileProvider.Builder().weightedData(list).radius
                                (50).gradient
                                (gradient).build();

                        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                    }
//                    Toast.makeText(getApplicationContext(),"Successfull",Toast.LENGTH_SHORT).show();
                } catch (JSONException e){
                    Log.i("StatusOfMapData","JSON error occurred");
                }


            }

            private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
                Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
                vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
                Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                vectorDrawable.draw(canvas);
                return BitmapDescriptorFactory.fromBitmap(bitmap);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i(TAG,"error occurred");
            }
        });

//        if (getMapData!=null){
//            for (int i=0; i < getMapData.length(); i++){
//                JSONObject object = getMapData.getJSONObject(i);
//                JSONObject fields = object.getJSONObject("fields");
//                double lat = fields.getDouble("latitude");
//                double lng = fields.getDouble("longitude");
//                double intensity = fields.getDouble("score");
//                WeightedLatLng weightedLatLng = new WeightedLatLng(new LatLng(lat,lng),intensity);
//                list.add(weightedLatLng);
//            }
//            return list;
//        }
//        // Call this method as an internal call and return the list for generating the heatmap
//        return null;
    }

    @Override
    protected void onDestroy() {
        killOtherActivity();
        super.onDestroy();
    }
}
