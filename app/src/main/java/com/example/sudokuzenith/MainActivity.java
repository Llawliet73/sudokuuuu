package com.example.sudokuzenith;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast; // ✅ FIXED: Added the missing import for Toast
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SwitchMaterial timerSwitch;
    private List<Button> difficultyButtons;
    private String selectedDifficulty = "Medium";
    private AdView adView;
    private static final String TAG = "MainActivityAds";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySavedTheme();
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, initializationStatus -> {});

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(TAG, "Ad successfully loaded and is now visible.");
                // This Toast will now work.
                Toast.makeText(MainActivity.this, "Test Ad Loaded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                Log.e(TAG, "Ad failed to load with error: " + adError.getMessage());
            }
        });
        
        adView.loadAd(adRequest);

        timerSwitch = findViewById(R.id.switch_timer);
        Button newGameButton = findViewById(R.id.btn_new_game);
        Button settingsButton = findViewById(R.id.btn_settings);

        setupDifficultyButtons();

        newGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("isTimerEnabled", timerSwitch.isChecked());
            intent.putExtra("difficulty", selectedDifficulty);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
    }

    private void applySavedTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode_enabled", true);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setupDifficultyButtons() {
        difficultyButtons = new ArrayList<>();
        Button easyBtn = findViewById(R.id.btn_easy_main);
        Button mediumBtn = findViewById(R.id.btn_medium_main);
        Button hardBtn = findViewById(R.id.btn_hard_main);
        difficultyButtons.add(easyBtn);
        difficultyButtons.add(mediumBtn);
        difficultyButtons.add(hardBtn);
        View.OnClickListener listener = v -> {
            for (Button b : difficultyButtons) {
                b.setSelected(false);
            }
            v.setSelected(true);
            selectedDifficulty = ((Button) v).getText().toString();
        };
        for (Button b : difficultyButtons) {
            b.setOnClickListener(listener);
        }
        mediumBtn.setSelected(true);
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}