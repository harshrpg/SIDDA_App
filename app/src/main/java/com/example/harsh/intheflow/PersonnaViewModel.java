package com.example.harsh.intheflow;

import androidx.lifecycle.ViewModel;

public class PersonnaViewModel extends ViewModel {

    // Purpose of view model is only to hold any UI data and no UI elements

    // Keeps the flag value
    private String flag;


    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
