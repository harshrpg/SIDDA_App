package com.example.harsh.intheflow;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.transition.Explode;
import android.transition.Fade;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import butterknife.BindDrawable;
import butterknife.BindView;

public class PersonnaActivity extends FragmentActivity {

    private String flag = null;
    private Drawable circle_check;
    private Drawable ic_energy;
    private Drawable ic_relaxing;
    private LinearLayout next_button;
    private TextView energetic_textview;
    private TextView relaxed_textview;
    private final String FILTER_FLAG = "FLAG";
    // A viewmodel to hold the UI data
    public PersonnaViewModel mViewModel;

    public static Activity personnaActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personna);
        personnaActivity = this;
        circle_check = this.getResources().getDrawable(R.drawable
                .ic_check_circle_black_24dp);
        ic_energy = this.getResources().getDrawable(R.drawable.ic_energy);
        ic_relaxing = this.getResources().getDrawable(R.drawable.ic_relax);
        next_button = findViewById(R.id.next_button);
        energetic_textview = findViewById(R.id.energetic);
        relaxed_textview = findViewById(R.id.relaxed);
        // Setting up the view model with this controller
        setupWindowAnimations();
        mViewModel = ViewModelProviders.of(this).get(PersonnaViewModel.class);
        displayElems(mViewModel.getFlag());
    }

    private void setupWindowAnimations() {
        Explode fade = new Explode();
        fade.setDuration(200);
        getWindow().setEnterTransition(fade);
    }

    public void setOptions(View view){
        flag = view.getTag().toString();
        mViewModel.setFlag(flag);
        displayElems(flag);
    }

    public void displayElems(String flag_vm){
        if ( flag_vm != null){
            next_button.setBackgroundTintList(this.getResources().getColorStateList(R.color.active));
            if (flag_vm.equals("energetic")) {
                energetic_textview.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable
                        .ic_check_circle_black_24dp,R.drawable.ic_energy,0,0);
                relaxed_textview.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable
                        .ic_relax,0,0);
            } else {
                energetic_textview.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable
                        .ic_energy,0,0);
                relaxed_textview.setCompoundDrawablesWithIntrinsicBounds(R.drawable
                        .ic_check_circle_black_24dp,R.drawable
                        .ic_relax,0,0);
            }
        }
    }

    public void skipToMap(View view){
        flag = null;
        mViewModel.setFlag(null);
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }

    public void setChoice(View view){
        if (flag != null && mViewModel.getFlag() != null){
            Intent intent = new Intent(this,MapsActivity.class);
            intent.putExtra(FILTER_FLAG,mViewModel.getFlag());
            startActivity(intent);
        }
    }
}
