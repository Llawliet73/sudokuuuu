package com.example.sudokuzenith;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sudokuzenith.view.SudokuBoard;

import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class GameActivity extends AppCompatActivity {

    private static class GameState {
        int row, col;
        int prevValue;
        boolean wasError;
        GameState(int row, int col, int prevValue, boolean wasError) {
            this.row = row; this.col = col; this.prevValue = prevValue; this.wasError = wasError;
        }
    }
    private SudokuBoard sudokuBoard;
    private SoundManager soundManager;
    private TextView timerText, errorsText;
    private LinearLayout timerContainer;
    private CountDownTimer timer;
    private int errorCount = 0;
    private String currentDifficulty;
    private int[][] puzzleInitial;
    private int[][] startingGrid;
    private int[][] solution;
    private int undoCount = 0;
    private boolean solverUsed = false;
    private long overtimeStartMillis = 0;
    private final Stack<GameState> undoStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize all views
        sudokuBoard = findViewById(R.id.sudokuBoard);
        timerContainer = findViewById(R.id.timer_container);
        timerText = findViewById(R.id.text_timer);
        errorsText = findViewById(R.id.text_errors);
        soundManager = new SoundManager(this);

        setupListeners();

        // Check if we are starting in review mode or starting a fresh game.
        if (getIntent().getBooleanExtra("start_in_review_mode", false)) {
            // This logic will run when coming back from the end screen
            // We don't load a new game, we just disable the controls.
            enterReviewMode();
        } else {
            // This logic runs for a brand new game from the main menu
            currentDifficulty = getIntent().getStringExtra("difficulty");
            boolean isTimerEnabled = getIntent().getBooleanExtra("isTimerEnabled", true);
            if (isTimerEnabled) {
                timerContainer.setVisibility(View.VISIBLE);
                startTimer();
            } else {
                timerContainer.setVisibility(View.GONE);
            }
            loadNewGame(currentDifficulty);
        }
    }

    private void setupListeners() {
        findViewById(R.id.btn_back_to_menu).setOnClickListener(v -> finish());
        findViewById(R.id.btn_undo).setOnClickListener(v -> undoLastMove());
        findViewById(R.id.btn_solve).setOnClickListener(v -> {
            solverUsed = true;
            if (solution != null) {
                sudokuBoard.setBoard(solution);
                sudokuBoard.setInteractionEnabled(false);
            }
        });
        findViewById(R.id.btn_check).setOnClickListener(v -> {
            if (solution != null) {
                sudokuBoard.checkBoard(solution);
            }
        });
        Switch pencilSwitch = findViewById(R.id.btn_pencil);
        pencilSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sudokuBoard.togglePencilMode();
            Toast.makeText(this, sudokuBoard.isPencilMode() ? "Pencil Mode On" : "Pencil Mode Off", Toast.LENGTH_SHORT).show();
        });

        // When New Game is clicked, it will now re-enable all the buttons.
        findViewById(R.id.btn_new_game_ingame).setOnClickListener(v -> {
            loadNewGame(currentDifficulty);
            exitReviewMode(); // Make sure all buttons are enabled
        });

        findViewById(R.id.btn_restart).setOnClickListener(v -> restartGame());
        for (int i = 1; i <= 9; i++) {
            int resId = getResources().getIdentifier("btn_" + i, "id", getPackageName());
            Button btn = findViewById(resId);
            if (btn != null) {
                final int number = i;
                btn.setOnClickListener(v -> onNumberClick(number));
            }
        }
        findViewById(R.id.btn_0).setOnClickListener(v -> {
            if(!sudokuBoard.isPencilMode()) {
                onNumberClick(0);
            } else {
                if (sudokuBoard.getSelectedRow() != -1 && sudokuBoard.getSelectedCol() != -1) {
                    sudokuBoard.removePencilMarks(sudokuBoard.getSelectedRow(), sudokuBoard.getSelectedCol());
                }
            }
        });
    }

    private void enterReviewMode() {
        // Stop the timer if it's running
        if (timer != null) {
            timer.cancel();
        }
        timerContainer.setVisibility(View.GONE); // Hide timer

        // Disable board interaction
        sudokuBoard.setInteractionEnabled(false);

        // Disable all buttons except "New Game"
        findViewById(R.id.btn_solve).setEnabled(false);
        findViewById(R.id.btn_check).setEnabled(false);
        findViewById(R.id.btn_undo).setEnabled(false);
        findViewById(R.id.btn_restart).setEnabled(false);
        findViewById(R.id.btn_pencil).setEnabled(false);
        for (int i = 0; i <= 9; i++) {
            int resId = getResources().getIdentifier("btn_" + i, "id", getPackageName());
            if (findViewById(resId) != null) {
                findViewById(resId).setEnabled(false);
            }
        }
    }

    private void exitReviewMode() {
        // Re-enable board interaction
        sudokuBoard.setInteractionEnabled(true);

        // Re-enable all buttons
        findViewById(R.id.btn_solve).setEnabled(true);
        findViewById(R.id.btn_check).setEnabled(true);
        findViewById(R.id.btn_undo).setEnabled(true);
        findViewById(R.id.btn_restart).setEnabled(true);
        findViewById(R.id.btn_pencil).setEnabled(true);
        for (int i = 0; i <= 9; i++) {
            int resId = getResources().getIdentifier("btn_" + i, "id", getPackageName());
            if (findViewById(resId) != null) {
                findViewById(resId).setEnabled(true);
            }
        }

        // Show timer if it was enabled for this difficulty
        boolean isTimerEnabled = getIntent().getBooleanExtra("isTimerEnabled", true);
        if (isTimerEnabled) {
            timerContainer.setVisibility(View.VISIBLE);
        }
    }

    private void endGame(String result) {
        sudokuBoard.setInteractionEnabled(false);
        if (timer != null) timer.cancel();
        String timeOverStr = "";
        if (overtimeStartMillis > 0) {
            long elapsedMillis = System.currentTimeMillis() - overtimeStartMillis;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60;
            timeOverStr = String.format("%02d:%02d", minutes, seconds);
        }
        Intent intent = new Intent(this, GameEndActivity.class);
        intent.putExtra("result", result);
        intent.putExtra("errors", errorCount);
        intent.putExtra("undoCount", undoCount);
        intent.putExtra("solverUsed", solverUsed);
        intent.putExtra("timeLeft", timerText.getText().toString());
        intent.putExtra("timeOver", timeOverStr);
        startActivity(intent);
        // We do NOT call finish() here, so we can return to this screen in review mode
    }

    private void undoLastMove() {
        if (!undoStack.isEmpty()) {
            GameState lastState = undoStack.pop();
            undoCount++;
            boolean isCurrentlyError = sudokuBoard.getErrorGrid()[lastState.row][lastState.col];
            sudokuBoard.setCell(lastState.row, lastState.col, lastState.prevValue, lastState.wasError);
            if (isCurrentlyError && !lastState.wasError) {
                errorCount--;
            }
            if (errorCount < 0) errorCount = 0;
            updateErrorsText();
            soundManager.play("click");
        } else {
            Toast.makeText(this, "Nothing to undo!", Toast.LENGTH_SHORT).show();
        }
    }
    public void saveUndoState(int row, int col, int prevValue, boolean wasError) {
        undoStack.push(new GameState(row, col, prevValue, wasError));
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void onNumberClick(int number) {
        if (sudokuBoard.getSelectedRow() == -1 || sudokuBoard.getSelectedCol() == -1) {
            Toast.makeText(this, "Please select a cell first!", Toast.LENGTH_SHORT).show();
            return;
        }
        sudokuBoard.setNumber(number);
        soundManager.play("click");
    }
    private void restartGame(){
        if(puzzleInitial !=null){
            solverUsed = false;
            sudokuBoard.setInteractionEnabled(true);
            startingGrid= deepCopy(puzzleInitial);
            sudokuBoard.setBoard(startingGrid);
            boolean[][] prefilledGrid = new boolean[9][9];
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    prefilledGrid[r][c] = (startingGrid[r][c] != 0);
                    sudokuBoard.removePencilMarks(r,c);
                }
            }
            sudokuBoard.setPrefilled(prefilledGrid);
            errorCount = 0;
            updateErrorsText();
            undoStack.clear();
            if(timerContainer.getVisibility() == View.VISIBLE) startTimer();
        }
    }
    private void loadNewGame(String difficulty) {
        if (difficulty == null) {
            // Handle case where we return from review mode without a set difficulty
            // Default to Medium or get from SharedPreferences if available
            currentDifficulty = "Medium"; 
        } else {
            currentDifficulty = difficulty;
        }
        
        solverUsed = false;
        sudokuBoard.setInteractionEnabled(true);
        solution = SudokuGenerator.generateSolvedBoard();
        startingGrid = deepCopy(solution);
        SudokuGenerator.removeCells(startingGrid, currentDifficulty);
        puzzleInitial = deepCopy(startingGrid);
        boolean[][] prefilledGrid = new boolean[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                prefilledGrid[r][c] = (startingGrid[r][c] != 0);
                sudokuBoard.removePencilMarks(r,c);
            }
        }
        sudokuBoard.setPrefilled(prefilledGrid);
        sudokuBoard.setBoard(startingGrid);
        errorCount = 0;
        updateErrorsText();
        undoStack.clear();
        
        boolean isTimerEnabled = getIntent().getBooleanExtra("isTimerEnabled", true);
        if (isTimerEnabled) {
             timerContainer.setVisibility(View.VISIBLE);
             startTimer();
        }
    }
    public void incrementErrorCount() {
        errorCount++;
        updateErrorsText();
        if(errorCount >=3) {
            checkForEndGame();
        }
    }
    public void checkForEndGame(){
        int[][] board = sudokuBoard.getBoard();
        boolean isComplete = true;
        if(solution != null) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board[r][c] != solution[r][c]) {
                        isComplete = false;
                        break;
                    }
                }
                if(!isComplete) break;
            }
        } else {
            isComplete = false;
        }
        if(isComplete) {
            endGame("win");
        }
        else if(errorCount>=3){
            endGame("lose");
        }
    }
    private void updateErrorsText() {
        errorsText.setText("Errors: " + errorCount + "/3");
    }
    private void startTimer() {
        if (timer != null) timer.cancel();
        overtimeStartMillis = 0;
        timer = new CountDownTimer(600000, 1000) {
            public void onTick(long millisUntilFinished) { timerText.setText(formatTime(millisUntilFinished)); }
            public void onFinish() {
                timerText.setText("00:00");
                Toast.makeText(GameActivity.this, "You've run out of time!", Toast.LENGTH_LONG).show();
                startOvertimeClock();
            }
        }.start();
    }
    private void startOvertimeClock() {
        overtimeStartMillis = System.currentTimeMillis();
    }
    private String formatTime(long millis) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        if (soundManager != null) soundManager.release();
    }
    private int[][] deepCopy(int[][] original) {
        if (original == null) return null;
        int[][] copy = new int[original.length][];
        for(int i=0;i<original.length;i++){
            copy[i] = original[i].clone();
        }
        return copy;
    }
    public String getCurrentDifficulty() {
        return currentDifficulty;
    }
}