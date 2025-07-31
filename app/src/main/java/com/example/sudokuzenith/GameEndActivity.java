package com.example.sudokuzenith;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GameEndActivity extends AppCompatActivity {

    private ViewGroup confettiContainer;
    private ImageView resultIcon;
    private TextView resultTitle, resultStats;
    private Button btnPlayAgain, btnMainMenu;
    private FloatingActionButton fabShare;
    private View cardResult;

    private String result, timeLeft, timeOver;
    private int errors, undoCount;
    private boolean solverUsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_end);

        confettiContainer = findViewById(R.id.confetti_container);
        resultIcon = findViewById(R.id.iv_result_icon);
        resultTitle = findViewById(R.id.tv_result_title);
        resultStats = findViewById(R.id.tv_result_stats);
        btnPlayAgain = findViewById(R.id.btn_play_again);
        btnMainMenu = findViewById(R.id.btn_main_menu);
        fabShare = findViewById(R.id.fab_share);
        cardResult = findViewById(R.id.card_result);

        Intent intent = getIntent();
        result = intent.getStringExtra("result");
        errors = intent.getIntExtra("errors", 0);
        undoCount = intent.getIntExtra("undoCount", 0);
        solverUsed = intent.getBooleanExtra("solverUsed", false);
        timeLeft = intent.getStringExtra("timeLeft");
        timeOver = intent.getStringExtra("timeOver");

        updateUI();
        setupListeners();
        runEntryAnimations();
    }

    private void updateUI() {
        StringBuilder statsBuilder = new StringBuilder();
        if (timeLeft != null && !timeLeft.isEmpty()) {
            statsBuilder.append("Time Left: ").append(timeLeft).append("\n");
        }
        if (timeOver != null && !timeOver.isEmpty()) {
            statsBuilder.append("Time Over: ").append(timeOver).append("\n");
        }
        statsBuilder.append("Errors: ").append(errors).append("/3");
        statsBuilder.append("\nUndos: ").append(undoCount);

        resultStats.setText(statsBuilder.toString());

        if ("win".equals(result)) {
            resultIcon.setImageResource(R.drawable.ic_trophy);

            // ✅ FIXED: Restored your custom messages and emojis.
            if (solverUsed) {
                resultTitle.setText("Puzzle solved 👀" +
                " but you used the Solver " +
                "  👎🏻  😑  👎🏻");
                // No confetti animation for a solved puzzle.
            } else {
                resultTitle.setText("Congrats! 🥳" +
                " you solved it! " +
                " 👍🏻  😎  👍🏻");
                triggerConfetti(); // Confetti only for a genuine, unaided win.
            }
        } else {
            resultIcon.setImageResource(R.drawable.ic_sad_face);
            resultTitle.setText("Game Over");
            fabShare.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnPlayAgain.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("start_in_review_mode", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnMainMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        fabShare.setOnClickListener(v -> shareResult());
    }

    private void shareResult() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareMessage = "I just finished a puzzle on SudokuZenith!\n" + "My stats:\n" + resultStats.getText().toString();
        if (solverUsed) {
            shareMessage += "\n(I used the solver to finish!)";
        }
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My SudokuZenith Result!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share your result"));
    }

    private void runEntryAnimations() {
        cardResult.setAlpha(0f);
        cardResult.setTranslationY(100f);
        fabShare.setScaleX(0f);
        fabShare.setScaleY(0f);
        cardResult.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(200).start();
        if (fabShare.getVisibility() == View.VISIBLE) {
            fabShare.animate().scaleX(1f).scaleY(1f).setDuration(400).setStartDelay(800).start();
        }
    }

    private void triggerConfetti() {
        final int[] colors = {Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.RED};
        CommonConfetti.rainingConfetti(confettiContainer, colors).oneShot();
    }
}