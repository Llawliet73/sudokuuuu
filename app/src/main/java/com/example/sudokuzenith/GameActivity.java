package com.example.sudokuzenith;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sudokuzenith.view.SudokuBoard;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class GameActivity extends AppCompatActivity {

    private SudokuBoard sudokuBoard;
    private SoundManager soundManager;
    private final Stack<int[][]> undoStack = new Stack<>();
    private TextView timerText, errorsText;
    private LinearLayout timerContainer;
    private CountDownTimer timer;
    private int errorCount = 0;
    private String currentDifficulty;
    private int[][] puzzleInitial;
    private int[][] startingGrid;
    private int[][] solution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        boolean isTimerEnabled = getIntent().getBooleanExtra("isTimerEnabled", true);
        currentDifficulty = getIntent().getStringExtra("difficulty");

        sudokuBoard = findViewById(R.id.sudokuBoard);
        timerContainer = findViewById(R.id.timer_container);
        timerText = findViewById(R.id.text_timer);
        errorsText = findViewById(R.id.text_errors);
        soundManager = new SoundManager(this);

        setupListeners();
        loadNewGame(currentDifficulty);

        if (isTimerEnabled) {
            timerContainer.setVisibility(View.VISIBLE);
            startTimer();
        } else {
            timerContainer.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        findViewById(R.id.btn_back_to_menu).setOnClickListener(v -> finish());
        findViewById(R.id.btn_undo).setOnClickListener(v -> undoLastMove());
        findViewById(R.id.btn_solve).setOnClickListener(v -> {
            if (solution != null) sudokuBoard.setBoard(solution);
        });
        findViewById(R.id.btn_check).setOnClickListener(v -> {
            if (solution != null) sudokuBoard.checkBoard(solution);
        });

        // ✅ FIXED: The listener for the in-game New Game button is now correctly added.
        findViewById(R.id.btn_new_game_ingame).setOnClickListener(v -> loadNewGame(currentDifficulty));
        findViewById(R.id.btn_restart).setOnClickListener(v -> restartGame());
        for (int i = 1; i <= 9; i++) {
            int resId = getResources().getIdentifier("btn_" + i, "id", getPackageName());
            Button btn = findViewById(resId);
            if (btn != null) {
                final int number = i;
                btn.setOnClickListener(v -> onNumberClick(number));
            }
        }
        findViewById(R.id.btn_0).setOnClickListener(v -> onNumberClick(0));
    }

    private void onNumberClick(int number) {
        sudokuBoard.setNumber(number);
        soundManager.play("click");
    }
    private void restartGame() {
        if (puzzleInitial != null) {
            startingGrid = deepCopy(puzzleInitial);

            // ✅ Rebuild prefilled grid on restart
            boolean[][] prefilledGrid = new boolean[9][9];
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    prefilledGrid[r][c] = (startingGrid[r][c] != 0);
                }
            }

            sudokuBoard.setBoard(startingGrid);
            sudokuBoard.setPrefilled(prefilledGrid);

            errorCount = 0;
            updateErrorsText();
            undoStack.clear();
            if (timerContainer.getVisibility() == View.VISIBLE) startTimer();
        }
    }

    private void loadNewGame(String difficulty) {
        solution = SudokuGenerator.generateSolvedBoard();
        startingGrid = deepCopy(solution);
        SudokuGenerator.removeCells(startingGrid, difficulty);
        puzzleInitial = deepCopy(startingGrid);
//
//        if ("Easy".equals(difficulty)) {
//            startingGrid = new int[][]{{6,0,0,1,9,5,0,0,0},{0,9,8,0,0,0,0,6,0},{5,3,0,0,7,0,0,0,0},{8,0,0,0,6,0,0,0,3},{4,0,0,8,0,3,0,0,1},{7,0,0,0,2,0,0,0,6},{0,6,0,0,0,0,2,8,0},{0,0,0,4,1,9,0,0,5},{0,0,0,0,8,0,0,7,9}};
//            solution = new int[][]{{6,2,4,1,9,5,3,7,8},{1,9,8,3,4,7,5,6,2},{5,3,7,6,2,8,1,4,9},{8,1,2,7,6,4,9,5,3},{4,5,9,8,3,2,6,1,7},{7,6,3,5,1,9,8,2,4},{9,4,5,7,6,3,1,8,2},{2,8,6,9,5,1,7,3,4},{1,7,3,4,8,2,9,5,6}};
//        } else if ("Hard".equals(difficulty)) {
//            startingGrid = new int[][]{{8,0,0,0,0,0,0,0,0},{0,0,3,6,0,0,0,0,0},{0,7,0,0,9,0,2,0,0},{0,5,0,0,0,7,0,0,0},{0,0,0,0,4,5,7,0,0},{0,0,0,1,0,0,0,3,0},{0,0,1,0,0,0,0,6,8},{0,0,8,5,0,0,0,1,0},{0,9,0,0,0,0,4,0,0}};
//            solution = new int[][]{{8,1,2,7,5,3,6,4,9},{9,4,3,6,8,2,1,7,5},{6,7,5,4,9,1,2,8,3},{1,5,4,2,3,7,8,9,6},{3,6,9,8,4,5,7,2,1},{2,8,7,1,6,9,5,3,4},{5,2,1,9,7,4,3,6,8},{4,3,8,5,2,6,9,1,7},{7,9,6,3,1,8,4,5,2}};
//        } else { // Medium
//            startingGrid = new int[][]{{5,3,0,0,7,0,0,0,0},{6,0,0,1,9,5,0,0,0},{0,9,8,0,0,0,0,6,0},{8,0,0,0,6,0,0,0,3},{4,0,0,8,0,3,0,0,1},{7,0,0,0,2,0,0,0,6},{0,6,0,0,0,0,2,8,0},{0,0,0,4,1,9,0,0,5},{0,0,0,0,8,0,0,7,9}};
//            solution = new int[][]{{5,3,4,6,7,8,9,1,2},{6,7,2,1,9,5,3,4,8},{1,9,8,3,4,2,5,6,7},{8,5,9,7,6,1,4,2,3},{4,2,6,8,5,3,7,9,1},{7,1,3,9,2,4,8,5,6},{9,6,1,5,3,7,2,8,4},{2,8,7,4,1,9,6,3,5},{3,4,5,2,8,6,1,7,9}};
//        }

//        boolean[][] prefilledGrid = new boolean[9][9];
//        for (int r = 0; r < 9; r++) {
//            for (int c = 0; c < 9; c++) {
//                prefilledGrid[r][c] = (startingGrid[r][c] != 0);
//            }
//        }
//        sudokuBoard.setPrefilled(prefilledGrid);
        // ✅ Mark prefilled cells
        boolean[][] prefilledGrid = new boolean[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                prefilledGrid[r][c] = (startingGrid[r][c] != 0);
            }
        }

        sudokuBoard.setBoard(startingGrid);
        sudokuBoard.setPrefilled(prefilledGrid);

        errorCount = 0;
        updateErrorsText();
        undoStack.clear();
        if (timerContainer.getVisibility() == View.VISIBLE) startTimer();
    }


    public void incrementErrorCount() {
        errorCount++;
        updateErrorsText();
    }

    private void updateErrorsText() {
        errorsText.setText("Errors: " + errorCount + "/3");
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(600000, 1000) {
            public void onTick(long millisUntilFinished) { timerText.setText(formatTime(millisUntilFinished)); }
            public void onFinish() { timerText.setText("00:00"); }
        }.start();
    }

    private String formatTime(long millis) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private void undoLastMove() {
        if (!undoStack.isEmpty()) {
            sudokuBoard.setBoard(undoStack.pop());
            soundManager.play("click");
        } else {
            Toast.makeText(this, "Nothing to undo!", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveUndoState(int[][] currentState) {
        undoStack.push(cloneBoard(currentState));
    }

    private int[][] cloneBoard(int[][] src) {
        if (src == null) return null;
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) System.arraycopy(src[i], 0, copy[i], 0, 9);
        return copy;
    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        if (soundManager != null) soundManager.release();
    }
    private int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for(int i=0;i<original.length;i++){
            copy[i] = original[i].clone();
        }
        return copy;
    }
}
