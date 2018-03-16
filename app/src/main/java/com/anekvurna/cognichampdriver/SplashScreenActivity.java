package com.anekvurna.cognichampdriver;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        CountDownTimer myTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                SanathUtilities.loadActivityAndFinish(SplashScreenActivity.this, UserChoiceActivity.class);
            }
        };
        myTimer.start();
    }
}
