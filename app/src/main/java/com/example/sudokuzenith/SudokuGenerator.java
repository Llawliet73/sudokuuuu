package com.example.sudokuzenith;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SudokuGenerator {
    private static int countSolutions(int [][] board) {
        return countHelper(board, 0, 0, 0);
    }
    private static int countHelper(int [][]board,int row,int col, int count) {
        if (count > 400) return count;
        if (row == 9) return count + 1;

        int nextRow = (col == 8) ? row + 1 : row;
        int nextCol = (col + 1) % 9;
//c
        if (board[row][col] != 0) {
            return countHelper(board, nextRow, nextCol, count);
        }

        for(int num =1;num<=9;num++) {
            if(isSafe(board,row,col,num)){
                board[row][col]  = num;
                count = countHelper(board,nextRow,nextCol,count);
                board[row][col] = 0;
            }
        }
        return count;
    }
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
    private static final int size = 9;

    // A pre-made solved puzzle to use as a base.
//    private static final int[][] BASE_PUZZLE = {
//            {5, 3, 4, 6, 7, 8, 9, 1, 2},
//            {6, 7, 2, 1, 9, 5, 3, 4, 8},
//            {1, 9, 8, 3, 4, 2, 5, 6, 7},
//            {8, 5, 9, 7, 6, 1, 4, 2, 3},
//            {4, 2, 6, 8, 5, 3, 7, 9, 1},
//            {7, 1, 3, 9, 2, 4, 8, 5, 6},
//            {9, 6, 1, 5, 3, 7, 2, 8, 4},
//            {2, 8, 7, 4, 1, 9, 6, 3, 5},
//            {3, 4, 5, 2, 8, 6, 1, 7, 9}
//    };

    public static int [][] generateSolvedBoard() {
        int[][] board = new int[size][size];
        solve(board);
        return board;
    }

    private static boolean solve(int [][] board){
        for(int row=0;row<size;row++){
            for(int col=0;col<size;col++){
                if(board[row][col] == 0){
                    ArrayList<Integer> numbers = getShuffledNumbers();
                    for(int num:numbers){
                        if(isSafe(board,row,col,num)){
                            board[row][col]=num;
                            if(solve(board)) return true;
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isSafe(int[][] board, int row, int col, int num){

        for(int i=0;i<size;i++)
            if(board[row][i] == num || board[i][col] == num) return false;
        int startRow = (row/3)*3;
        int startCol = (col/3)*3;       //Get the top leftmost corner of the 3x3 grid

        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                if(board[startRow+i][startCol+j]==num) return false;
            }
        }
        return true;
    }
    private static ArrayList<Integer> getShuffledNumbers()  {
        ArrayList <Integer> numbers = new ArrayList<>();
        for(int i=1;i<=size;i++){
            numbers.add(i);
        }
        Collections.shuffle(numbers,new Random());
        return numbers;
    }
    public static void removeCells(int[][] puzzle,String difficulty) {
        Difficulty level = Difficulty.valueOf(difficulty.toUpperCase());

        //        for (int i = 0; i < 9; i++) {
//            System.arraycopy(BASE_PUZZLE[i], 0, puzzle[i], 0, 9);
//        }

        int cellsToRemove = 0;
        switch (level) {
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
//        for (int i = 0; i < cellsToRemove; i++) {
//            int row = random.nextInt(9);
//            int col = random.nextInt(9);
//
//            if (puzzle[row][col] != 0) {
//                puzzle[row][col] = 0;
//            } else {
//                // If the cell is already empty, try again.
//                i--;
//            }
//        }

//        while(cellsToRemove > 0) {
//            int row = random.nextInt(9);
//            int col = random.nextInt(9);
//            if(puzzle[row][col] !=0){
//                puzzle[row][col] = 0;
//                cellsToRemove--;
//            }
//        }
        if(!difficulty.equalsIgnoreCase("HARD")) {
            while (cellsToRemove > 0) {
                int row = random.nextInt(9);
                int col = random.nextInt(9);

                if (puzzle[row][col] != 0) {
                    int backup = puzzle[row][col];
                    puzzle[row][col] = 0;

                    int[][] copy = deepCopy(puzzle);
                    if (countSolutions(copy) == 1) {
                        cellsToRemove--;
                    } else {
                        puzzle[row][col] = backup;
                    }
                }
            }
        }
        else{
            while (cellsToRemove > 0) {
                int row = random.nextInt(9);
                int col = random.nextInt(9);

                if (puzzle[row][col] != 0) {
                    puzzle[row][col] = 0;

                    int[][] copy = deepCopy(puzzle);
                    cellsToRemove--;
                }
            }
        }
//        Log.d("difficultyDebug", String.valueOf(difficulty.equals("HARD")));

//        return puzzle;
    }

    private static int[][] deepCopy(int[][] board) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 9);
        }
        return copy;
    }



}