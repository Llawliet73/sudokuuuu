package com.example.sudokuzenith;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sudokuzenith.view.SudokuBoard;

import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class GameActivity extends AppCompatActivity {

    private SudokuBoard sudokuBoard;
    private SoundManager soundManager;
    private final Stack<GameState> undoStack = new Stack<>();
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
        findViewById(R.id.btn_undo).setOnClickListener(v -> {
            undoLastMove();
//            if(sudokuBoard.errorCells)
        });
        findViewById(R.id.btn_solve).setOnClickListener(v -> {
            solverUsed = true;
            if (solution != null) sudokuBoard.setBoard(solution);
        });
        findViewById(R.id.btn_check).setOnClickListener(v -> {
            if (solution != null) {
               sudokuBoard.checkBoard(solution);
            }
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
    private static class GameState {
        int row,col;
        int prevValue;
        boolean wasError;

        GameState(int row,int col, int prevValue, boolean wasError) {
            this.row = row;
            this.col = col;
            this.prevValue = prevValue;
            this.wasError = wasError;
        }
//        int [][] boardState;
//        boolean [][] errorState;
//        GameState(int[][] boardState,boolean[][] errorState) {
//            this.boardState = boardState;
//            this.errorState = errorState;
//        }
    }
    private void onNumberClick(int number) {
        sudokuBoard.setNumber(number);
        soundManager.play("click");
    }
    private void restartGame(){
        if(puzzleInitial !=null){
            startingGrid= deepCopy(puzzleInitial);
            sudokuBoard.setBoard(startingGrid);
            errorCount = 0;
            updateErrorsText();
            undoStack.clear();
            if(timerContainer.getVisibility() == View.VISIBLE) startTimer();
        }
    }
    private void loadNewGame(String difficulty) {
        solution = SudokuGenerator.generateSolvedBoard();
        startingGrid = deepCopy(solution);
        SudokuGenerator.removeCells(startingGrid,difficulty);
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
        sudokuBoard.setBoard(startingGrid);
        errorCount = 0;
        updateErrorsText();
        undoStack.clear();
        if (timerContainer.getVisibility() == View.VISIBLE) startTimer();
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
        for(int r=0;r<9;r++){
            for(int c=0;c<9;c++){
                if(board[r][c] != solution[r][c]){
                    isComplete = false;
                    break;
                }
            }
        }

        if(isComplete) {
            endGame("win");
        }
        else if(errorCount>=3){
            endGame("lose");
        }
    }
    private void endGame(String result) {
        String timeOver = "";
        if(overtimeStartMillis>0) {
            long elapsedMillis = System.currentTimeMillis() - overtimeStartMillis;
            long secondsOver = (int) (elapsedMillis/1000);
            long minutes = secondsOver/60;
            long seconds = secondsOver % 60;
            timeOver = String.format("%02d:%02d",minutes, seconds);
        }
        Intent intent = new Intent(this, GameEndActivity.class);
        intent.putExtra("result", result);
        intent.putExtra("errors",errorCount);
        intent.putExtra("undoCount",undoCount);
        intent.putExtra("solverUsed",solverUsed);
        intent.putExtra("timeOver",timeOver);
        if(timer!=null)
            intent.putExtra("timeLeft",timerText.getText().toString());
        startActivity(intent);
        finish();
    }
    private void updateErrorsText() {
        errorsText.setText("Errors: " + errorCount + "/3");
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(600000, 1000) {
            public void onTick(long millisUntilFinished) { timerText.setText(formatTime(millisUntilFinished)); }
            public void onFinish() { timerText.setText("00:00");
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

    private void undoLastMove() {
        if (!undoStack.isEmpty()) {
            GameState state = undoStack.pop();
            undoCount++;
//            int currentErrors = countTrueValues(sudokuBoard.getErrorGrid());
            // Get current value before undo for comparison
            int currentVal = sudokuBoard.getBoard()[state.row][state.col];
            boolean isCurrentlyError = sudokuBoard.getErrorGrid()[state.row][state.col];

            // Undo the value and error status
            sudokuBoard.setCell(state.row, state.col, state.prevValue, state.wasError);

//            // Restore board and error cells
//            sudokuBoard.setBoard(previousState.boardState);
//            sudokuBoard.setErrorGrid(previousState.errorState);
//
//            int newErrors = countTrueValues(previousState.errorState);

            //Adjust error count
//            if (isCurrentlyError && !state.wasError) {
//                errorCount--;
//            } else if (!isCurrentlyError && state.wasError) {
//                errorCount++;
//            }

            sudokuBoard.setCell(state.row,state.col,state.prevValue,false);
            boolean shouldBeError = state.prevValue !=0  && state.prevValue != solution[state.row][state.col];
            sudokuBoard.setErrorCell(state.row,state.col,shouldBeError);
            if (isCurrentlyError && !shouldBeError) {
                errorCount--;
            } else if (!isCurrentlyError && shouldBeError) {
                errorCount++;
            }
//            errorCount -= (currentErrors - newErrors);
            if (errorCount < 0) errorCount = 0;

            updateErrorsText();

            soundManager.play("click");
        } else {
            Toast.makeText(this, "Nothing to undo!", Toast.LENGTH_SHORT).show();
        }
    }

//    public void saveUndoState(int[][] currentState) {
//        boolean[][] errorState = sudokuBoard.getErrorGrid();
//        undoStack.push(new GameState(deepCopy(currentState),deepCopy(errorState)));
//    }
//    public void saveUndoState(int[][] currentState,boolean[][] currentErrors) {
//        undoStack.push(new GameState(deepCopy(currentState),deepCopy(currentErrors)));
//    }
    public void saveUndoState(int row, int col, int prevValue, boolean wasError) {
        undoStack.push(new GameState(row, col, prevValue, wasError));
    }


//    private int[][] cloneBoard(int[][] src) {
//        if (src == null) return null;
//        int[][] copy = new int[9][9];
//        for (int i = 0; i < 9; i++) System.arraycopy(src[i], 0, copy[i], 0, 9);
//        return copy;
//    }

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
    private boolean[][] deepCopy(boolean[][] original) {
        boolean[][] copy = new boolean[original.length][];
        for(int i=0;i<original.length;i++){
            copy[i] = original[i].clone();
        }
        return copy;
    }
    private int countTrueValues(boolean[][] array) {
        int count = 0;
        for (boolean[] row : array) {
            for (boolean value : row) {
                if (value) count++;
            }
        }
        return count;
    }
}