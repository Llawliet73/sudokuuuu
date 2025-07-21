//package com.example.sudokuzenith;
//
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class SudokuSolver {
//    //Add this to GameActivity onCreate
//
//    //Add this to the sudokuBoard class once complete
//
//    public static List<int[][]> getAllSolutions(int[][] board) {
//        List<int[][]> solutions = new ArrayList<>();
//        solve(board, 0, 0, solutions);
//        return solutions;
//    }
//
//    private static void solve(int[][] board, int row, int col, List<int[][]> solutions) {
//        if (row == 9) {
//            solutions.add(deepCopy(board));
//            return;
//        }
//
//        if (col == 9) {
//            solve(board, row + 1, 0, solutions);
//            return;
//        }
//
//        if (board[row][col] != 0) {
//            solve(board, row, col + 1, solutions);
//            return;
//        }
//
//        for (int num = 1; num <= 9; num++) {
//            if (isValid(board, row, col, num)) {
//                board[row][col] = num;
//                solve(board, row, col + 1, solutions);
//                board[row][col] = 0;
//            }
//        }
//    }
//
//    private static boolean isValid(int[][] board, int row, int col, int num) {
//        for (int i = 0; i < 9; i++) {
//            if (board[row][i] == num || board[i][col] == num) return false;
//        }
//
//        int boxRow = (row / 3) * 3;
//        int boxCol = (col / 3) * 3;
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                if (board[boxRow + i][boxCol + j] == num) return false;
//            }
//        }
//
//        return true;
//    }
////    private static boolean isValid(int[][] board, boolean[][] errorCells, int row, int col, int num) {
////        errorCells[row][col] = false;
////
////        for (int i = 0; i < 9; i++) {
////            if (board[row][i] == num || board[i][col] == num) {
////                errorCells[row][col] = true;
////                return false;
////            }
////        }
////
////        int boxRow = (row / 3) * 3;
////        int boxCol = (col / 3) * 3;
////        for (int i = 0; i < 3; i++) {
////            for (int j = 0; j < 3; j++) {
////                if (board[boxRow + i][boxCol + j] == num) {
////                    errorCells[row][col] = true;
////                    return false;
////                }
////            }
////        }
////
////        return true;
////    }
//
//
//    private static int[][] deepCopy(int[][] board) {
//        int[][] copy = new int[9][9];
//        for (int i = 0; i < 9; i++) {
//            copy[i] = board[i].clone();
//        }
//        return copy;
//    }
//}
