package com.uchur.uchur;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;

public class offlineScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_screen);

        Button exitButton = (Button) findViewById(R.id.button2);

        exitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                switchToMainActivity();
//                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    private void switchToMainActivity() {
        Intent switchActivityToMainIntent = new Intent(this, MainActivity.class);
        startActivity(switchActivityToMainIntent);
    }
}