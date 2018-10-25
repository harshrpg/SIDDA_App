package com.example.harsh.intheflow;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /**
    * Opens the map on click of the button
    * */
    public void openMap(View view){
        // Testing if the method work
        Snackbar.make(view,"Button clicked",Snackbar.LENGTH_LONG).show();
        // Make an intent to open another activity
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);

    }
}
