package com.example.harsh.intheflow;

import android.app.ActivityOptions;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setupWindowAnimations();

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
