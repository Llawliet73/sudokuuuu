package com.example.sudokuzenith.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.example.sudokuzenith.GameActivity;
import com.example.sudokuzenith.R;

public class SudokuBoard extends View {
    private final int[][] board = new int[9][9];
    private final boolean[][] prefilledCells = new boolean[9][9];
    private int selectedRow = -1, selectedCol = -1;
    private final boolean[][] errorCells = new boolean[9][9];

    private Paint thickLinePaint, thinLinePaint, selectedCellPaint, relatedCellPaint,
            prefilledTextPaint, userTextPaint, errorCellPaint, errorTextPaint;
    private float cellSize;
    private boolean isInitialized = false;

    public SudokuBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initPaints() {
        thickLinePaint = new Paint();
        thickLinePaint.setStyle(Paint.Style.STROKE);
        thickLinePaint.setStrokeWidth(8);
        thinLinePaint = new Paint();
        thinLinePaint.setStyle(Paint.Style.STROKE);
        thinLinePaint.setStrokeWidth(2);
        selectedCellPaint = new Paint();
        selectedCellPaint.setStyle(Paint.Style.FILL);
        relatedCellPaint = new Paint();
        relatedCellPaint.setStyle(Paint.Style.FILL);
        prefilledTextPaint = new Paint();
        prefilledTextPaint.setTextAlign(Paint.Align.CENTER);
        prefilledTextPaint.setAntiAlias(true);
        userTextPaint = new Paint();
        userTextPaint.setTextAlign(Paint.Align.CENTER);
        userTextPaint.setAntiAlias(true);
        errorCellPaint = new Paint();
        errorCellPaint.setStyle(Paint.Style.FILL);
        errorTextPaint = new Paint(userTextPaint);

        thickLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.boardLineColor));
        thinLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.boardLineColor));
        selectedCellPaint.setColor(ContextCompat.getColor(getContext(), R.color.boardSelectedCell));
        relatedCellPaint.setColor(ContextCompat.getColor(getContext(), R.color.boardRelatedCell));
        prefilledTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.textPrefilled));
        userTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.textUserFilled));
        errorCellPaint.setColor(ContextCompat.getColor(getContext(), R.color.statusError));
        errorTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        isInitialized = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInitialized) initPaints();
        if (getWidth() == 0) return;
        cellSize = getWidth() / 9f;
        prefilledTextPaint.setTextSize(cellSize * 0.7f);
        userTextPaint.setTextSize(cellSize * 0.7f);
        errorTextPaint.setTextSize(cellSize * 0.7f);
        drawBackground(canvas);
        drawGridLines(canvas);
        drawNumbers(canvas);
    }

    private void drawBackground(Canvas canvas) {
        if (selectedRow != -1 && selectedCol != -1) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (r == selectedRow || c == selectedCol || (r / 3 == selectedRow / 3 && c / 3 == selectedCol / 3)) {
                        canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, relatedCellPaint);
                    }
                }
            }
            canvas.drawRect(selectedCol * cellSize, selectedRow * cellSize, (selectedCol + 1) * cellSize, (selectedRow + 1) * cellSize, selectedCellPaint);
        }
    }

    private void drawGridLines(Canvas canvas) {
        for (int i = 0; i <= 9; i++) {
            Paint paint = (i % 3 == 0) ? thickLinePaint : thinLinePaint;
            canvas.drawLine(i * cellSize, 0, i * cellSize, getWidth(), paint);
            canvas.drawLine(0, i * cellSize, getWidth(), i * cellSize, paint);
        }
    }

    private void drawNumbers(Canvas canvas) {
        Rect textBounds = new Rect();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (errorCells[r][c]) {
                    canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, errorCellPaint);
                }
                if (board[r][c] != 0) {
                    String text = String.valueOf(board[r][c]);
                    Paint paint = errorCells[r][c] ? errorTextPaint : (prefilledCells[r][c] ? prefilledTextPaint : userTextPaint);
                    paint.getTextBounds(text, 0, text.length(), textBounds);
                    float x = c * cellSize + cellSize / 2f;
                    float y = r * cellSize + cellSize / 2f + textBounds.height() / 2f;
                    canvas.drawText(text, x, y, paint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (cellSize == 0) return false;
            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);
            if (row >= 0 && row < 9 && col >= 0 && col < 9) {
                selectedRow = row;
                selectedCol = col;
                invalidate();
                return true;
            }
        }
        return false;
    }

    public void setBoard(int[][] boardState) {
        for (int r = 0; r < 9; r++) System.arraycopy(boardState[r], 0, this.board[r], 0, 9);
        resetErrors();
        invalidate();
    }

    public void setPrefilled(boolean[][] prefilledState) {
        for (int r = 0; r < 9; r++) System.arraycopy(prefilledState[r], 0, this.prefilledCells[r], 0, 9);
    }

    public int[][] getBoard() {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) System.arraycopy(this.board[i], 0, copy[i], 0, 9);
        return copy;
    }

    public void setNumber(int number) {
        if (selectedRow != -1 && selectedCol != -1 && !prefilledCells[selectedRow][selectedCol]) {
            if (getContext() instanceof GameActivity) ((GameActivity) getContext()).saveUndoState(getBoard());
            board[selectedRow][selectedCol] = number;
            errorCells[selectedRow][selectedCol] = false;
            invalidate();
        }
    }

    // ✅ FIXED: A simpler check method that works with the GameActivity
    public void checkBoard(int[][] solution) {
        if (solution == null) return;
        resetErrors();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (!prefilledCells[r][c] && board[r][c] != 0 && board[r][c] != solution[r][c]) {
                    errorCells[r][c] = true;
                    if (getContext() instanceof GameActivity) {
                        ((GameActivity) getContext()).incrementErrorCount();
                    }
                }
            }
        }
        invalidate();
    }

    public void resetErrors() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) errorCells[r][c] = false;
        }
        invalidate();
    }
}