package com.example.harsh.intheflow;

import android.os.Bundle;
import android.app.Activity;
import android.transition.Explode;
import android.transition.Fade;

public class PersonnaActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personna);
        setupWindowAnimations();
    }

    private void setupWindowAnimations() {
        Explode fade = new Explode();
        fade.setDuration(200);
        getWindow().setEnterTransition(fade);
    }

}
