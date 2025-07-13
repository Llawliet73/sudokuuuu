package com.example.sudokuzenith;

public class SudokuSolver {

    private int[][] board;
    private int[][] solution;
    private boolean solved;

    public SudokuSolver(int[][] board) {
        this.board = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, 9);
        }
        this.solution = new int[9][9];
        this.solved = false;
    }

    public int[][] getSolution() {
        if (!solved) {
            solve();
        }
        return solution;
    }

    private boolean solve() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                // Find an empty cell
                if (board[row][col] == 0) {
                    // Try numbers 1 through 9
                    for (int num = 1; num <= 9; num++) {
                        if (isSafe(row, col, num)) {
                            board[row][col] = num;
                            // Recurse
                            if (solve()) {
                                // If solved, copy to solution and return true
                                for (int i = 0; i < 9; i++) {
                                    System.arraycopy(board[i], 0, solution[i], 0, 9);
                                }
                                solved = true;
                                return true;
                            }
                            // Backtrack if the recursion did not lead to a solution
                            board[row][col] = 0;
                        }
                    }
                    // If no number works, this path is incorrect
                    return false;
                }
            }
        }
        // If we get here, the board is full and solved
        for (int i = 0; i < 9; i++) {
            System.arraycopy(board[i], 0, solution[i], 0, 9);
        }
        solved = true;
        return true;
    }

    private boolean isSafe(int row, int col, int num) {
        // Check if 'num' is not in the current row and column
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == num || board[i][col] == num) {
                return false;
            }
        }

        // Check if 'num' is not in the current 3x3 sub-grid
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i + startRow][j + startCol] == num) {
                    return false;
                }
            }
        }

        return true;
    }
}
