package com.example.nba_naac_ems;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase Manually using the API key from BuildConfig
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FIREBASE_API_KEY)
                .setApplicationId("1:109664087459:android:3aa6868e6a10ab51307e16")
                .setProjectId("nba-naac-ems")
                .setDatabaseUrl("https://nba-naac-ems-default-rtdb.firebaseio.com/")
                .setStorageBucket("nba-naac-ems.firebasestorage.app")
                .build();

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this, options);
        }

        // Find the root layout to animate
        RelativeLayout splashRoot = findViewById(R.id.splashRoot);

        // Load and start animation
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_anim);
        splashRoot.startAnimation(animation);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_TIME);
    }
}