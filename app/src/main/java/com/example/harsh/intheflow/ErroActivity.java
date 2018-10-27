package com.example.harsh.intheflow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class ErroActivity extends AppCompatActivity {
    private final String ERROR_CONNECTIVITY = "Internet";
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erro);
        errorTextView = findViewById(R.id.errorText);
        Intent intent = getIntent();

        String test = (String)intent.getStringExtra(ERROR_CONNECTIVITY);
        errorTextView.setText(test);
    }

    /**
     * This function opens the settings if selected by the user
     * @param view
     */
    public void openSettings(View view){
        Intent intent_mess = getIntent();
        if (intent_mess.getExtras() != null && intent_mess.hasExtra(ERROR_CONNECTIVITY)){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
            startActivity(intent);
        }

    }

    /**
     * Closes the app on view click event
     * @param view
     */
    public void closeApp(View view){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
