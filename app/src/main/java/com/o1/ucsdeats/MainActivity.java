package com.o1.ucsdeats;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sets a delay in order to view Splash Screen
        Thread splashThread = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                    Intent homeActivity = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(homeActivity);
                    finish();
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                }
            }
        };

        splashThread.start();
    }
}
