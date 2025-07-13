package com.example.sudokuzenith;

import java.util.Random;

// A very basic Sudoku puzzle generator.
public class SudokuGenerator {

    // A pre-made solved puzzle to use as a base.
    private static final int[][] BASE_PUZZLE = {
            {5, 3, 4, 6, 7, 8, 9, 1, 2},
            {6, 7, 2, 1, 9, 5, 3, 4, 8},
            {1, 9, 8, 3, 4, 2, 5, 6, 7},
            {8, 5, 9, 7, 6, 1, 4, 2, 3},
            {4, 2, 6, 8, 5, 3, 7, 9, 1},
            {7, 1, 3, 9, 2, 4, 8, 5, 6},
            {9, 6, 1, 5, 3, 7, 2, 8, 4},
            {2, 8, 7, 4, 1, 9, 6, 3, 5},
            {3, 4, 5, 2, 8, 6, 1, 7, 9}
    };

    public static int[][] generatePuzzle(Difficulty difficulty) {
        int[][] puzzle = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(BASE_PUZZLE[i], 0, puzzle[i], 0, 9);
        }

        int cellsToRemove = 0;
        switch (difficulty) {
            case EASY:
                cellsToRemove = 40;
                break;
            case MEDIUM:
                cellsToRemove = 50;
                break;
            case HARD:
                cellsToRemove = 60;
                break;
        }

        Random random = new Random();
        for (int i = 0; i < cellsToRemove; i++) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);

            if (puzzle[row][col] != 0) {
                puzzle[row][col] = 0;
            } else {
                // If the cell is already empty, try again.
                i--;
            }
        }

        return puzzle;
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}