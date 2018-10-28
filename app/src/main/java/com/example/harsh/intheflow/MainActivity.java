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


    public void openPersonna(View view){
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        Intent intent = new Intent(this, PersonnaActivity.class);
        startActivity(intent,options.toBundle());
        finish();
    }
}
