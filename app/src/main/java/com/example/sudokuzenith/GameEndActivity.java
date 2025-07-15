package com.example.sudokuzenith;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameEndActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_end);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        String result = getIntent().getStringExtra("result");
        int errors = getIntent().getIntExtra("errors",0);
        int undoCount = getIntent().getIntExtra("undoCount",0);
        boolean solverUsed = getIntent().getBooleanExtra("solverUsed",false);
        String timeLeft = getIntent().getStringExtra("timeLeft");
        String timeOver = getIntent().getStringExtra("timeOver");
        TextView endMessage = findViewById(R.id.endMessage);
        StringBuilder message = new StringBuilder();

        if("win".equals(result)) {
            if(solverUsed){
                message.append("You finished the puzzle...\nBut yOU used the Solver \uD83D\uDC40\n");
            } else {
                message.append("Woohoo! You finished the puzzle! 🎉\n");
            }
            message.append("Errors made: ").append(errors).append("\n");
        }
        else {
            message.append("Game Over \uD83D\uDE22\n");
            message.append("You got over 3 errors\n");
        }
        message.append("Number of times undo used: ").append(undoCount).append("\n");
        if(timeOver != null && !timeOver.isEmpty()) {
            message.append("You went over time by: ").append(timeOver).append("\n");
        } else if(timeLeft!=null) {
            message.append("Time left: ").append(timeLeft).append("\n");
        }
        endMessage.setText(message.toString());

        findViewById(R.id.btn_back_to_menu).setOnClickListener(v -> {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        });
    }
}