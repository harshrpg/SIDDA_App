package com.example.harsh.intheflow;

import android.os.Bundle;
import android.app.Activity;
import android.transition.Fade;

public class PersonnaActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personna);
        setupWindowAnimations();
    }

    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
    }

}
