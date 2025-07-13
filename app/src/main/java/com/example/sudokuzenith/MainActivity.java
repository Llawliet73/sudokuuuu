package com.example.sudokuzenith;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SwitchMaterial timerSwitch;
    private List<Button> difficultyButtons;
    private String selectedDifficulty = "Medium"; // Default difficulty

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ This is critical: Apply the saved theme BEFORE the layout is created.
        applySavedTheme();

        setContentView(R.layout.activity_main);

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
        // Default to true (Dark Mode)
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
}