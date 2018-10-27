package com.example.harsh.intheflow;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.transition.Slide;
import android.transition.Transition;
import androidx.transition.TransitionInflater;
import androidx.transition.TransitionManager;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private final String ERROR_CONNECTIVITY = "Internet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Step 1. Check if the user has a network connection
        if (isNetworkAvailable()){
            // Step 2. If the internet connection is on and stable
            // Check shared preferences for the app and display splash screen is needed else move
            // to the final map
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences
                    ("App_Pref",Context.MODE_PRIVATE);
            // Always maintain private mode for in house checks
            if (sharedPreferences.getBoolean("activity_started",false)){
                Intent intent = new Intent(this,MapsActivity.class);
                startActivity(intent);
            } else {
                SharedPreferences.Editor ed = sharedPreferences.edit();
                ed.putBoolean("activity_started", true);
                ed.commit();
            }
        } else {
            // If network not present show error
            Intent intent = new Intent(this, ErroActivity.class);
            intent.putExtra(ERROR_CONNECTIVITY,"No Internet Connection");
            startActivity(intent);
        }

    }


    /**
     * This function checks for a valid internet connection
     * needs a permission for ACCESS_NETWORK_STATE from manifest
     * @return boolean to represent whether user has a network or not
     */
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void setupWindowAnimations() {
        Toast.makeText(this, "Setting up",Toast.LENGTH_LONG).show();
        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setExitTransition(slide);
    }


    public void openPersonna(View view){
        Snackbar.make(view,"Button clicked",Snackbar.LENGTH_LONG).show();
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        Intent intent = new Intent(this, PersonnaActivity.class);
        startActivity(intent,options.toBundle());
    }
    /**
    * Opens the map on click of the button
    * */
    public void openMap(View view){
        // Testing if the method work
        Snackbar.make(view,"Button clicked",Snackbar.LENGTH_LONG).show();
        // Make an intent to open another activity

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent,ActivityOptions.makeSceneTransitionAnimation(this).toBundle());

    }
}
