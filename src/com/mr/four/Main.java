package com.mr.four;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

	private static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
		System.out.println("\nGreetings, Professor Falken. Shall we play a game?");
	    start();
    }

	// Board Definition

	// Note: we prefer 7x7 to the more common 6x7 as it offers better Zugzwang opportunities

	private static final byte SPACE = 0;
	private static final byte WHITE = 1;
	private static final byte BLACK = 2;
	private static final byte FRAME = 3;

	private static final byte[] BOARD = new byte[] {
			FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME,
			FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME,
			FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, FRAME, FRAME, FRAME,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, FRAME, FRAME, FRAME,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, FRAME, FRAME, FRAME,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, FRAME, FRAME, FRAME,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, FRAME, FRAME, FRAME,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, FRAME, FRAME, FRAME,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, FRAME, FRAME, FRAME,
			FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME,
			FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME,
			FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME, FRAME
	};

	private static final int[] TOP = new int[] { 0, 0, 0, 0, 0, 0, 0 };

	private static final byte ROWS = 7;
	private static final byte COLUMNS = 7;

	private static int index(int row, int column) {
		return (COLUMNS + 3) * (row + 3) + column;
	}

	private static byte[] board = new byte[BOARD.length];

	private static int[] top = new int[TOP.length];

	private static void drop(int column, byte color) {
		board[index(top[column]++, column)] = color;
	}

	private static boolean isOption(int column) {
		return column >= 0 && column < COLUMNS && top[column] < ROWS;
	}

	private static List<Integer> getOptions() throws Exception {
		ArrayList<Integer> options = new ArrayList<>(COLUMNS);
		for (int c = 0; c < COLUMNS; c++)
			if (top[c] < ROWS)
				options.add(c);
		if (options.isEmpty())
			throw new Exception();
		return options;
	}

	// Game Loop

	private static void start() {
		System.arraycopy(BOARD, 0, board, 0, BOARD.length);
		System.arraycopy(TOP, 0, top, 0, TOP.length);
		try {
			loop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void loop() throws Exception {
		byte player = WHITE;
		byte winner;
		do {
			printBoard();
			printValue();
			int column = player == WHITE ? strategyUser(player) : strategyRandom(player);
			drop(column, player);
			winner = winner(column);
			player = (byte) (3 - player);
		}
		while (winner == SPACE);
		printBoard();
		printValue();
		switch (winner) {
			case WHITE: System.out.println("You win.\n"); break;
			case BLACK: System.out.println("You lose.\n"); break;
			default: System.out.println("It's a draw.\n");
		}
	}

	private static int strategyUser(byte color) {
		for (;;) {
			int column = getUserInput(color);
			if (isOption(column))
				return column;
			System.out.println("Illegal move. Try again.");
		}
	}

	private static int strategyRandom(byte color) throws Exception {
		List<Integer> options = getOptions();
		return options.get((int) (Math.random() * options.size()));
	}

	private static int strategySearch(byte color) throws Exception {
		return color == WHITE ? white(Integer.MIN_VALUE, Integer.MAX_VALUE) : black(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	// User Interaction

	private static void printBoard() {
		System.out.println("\n  1 2 3 4 5 6 7   ");
		for (int r = ROWS - 1; r >= 0; r--) {
			StringBuilder line = new StringBuilder();
			int base = index(r, -1);
			for (int c = -1; c <= COLUMNS; c++) {
				switch (board[base++]) {
					case SPACE: line.append("  "); break;
					case WHITE: line.append("o "); break;
					case BLACK: line.append("x "); break;
					default: line.append("| ");
				}
			}
			System.out.println(line.toString());
		}
		System.out.println("----------------- \n");
	}

	private static void printValue() throws Exception {
		System.out.println("\nValue: " + value() + "\n");
	}

	private static int getUserInput(byte color) {
		System.out.print("Your move for " + (color == WHITE ? "white: " : "black: "));
		return in.nextInt() - 1;
	}

	// Valuation, specific to a 7x7 board

	private static final int[] UL0 = new int[] { 0, 9, 18, 27 };
	private static final int[] UL1 = new int[] { -9, 0, 9, 18 };
	private static final int[] UL2 = new int[] { -18, -9, 0, 9 };
	private static final int[] UL3 = new int[] { -27, -18, -9, 0 };

	private static final int[] U0 = new int[] { 0, 10, 20, 30 };
	private static final int[] U1 = new int[] { -10, 0, 10, 20 };
	private static final int[] U2 = new int[] { -20, -10, 0, 10 };
	private static final int[] U3 = new int[] { -30, -20, -10, 0 };

	private static final int[] UR0 = new int[] { 0, 11, 22, 33 };
	private static final int[] UR1 = new int[] { -11, 0, 11, 22 };
	private static final int[] UR2 = new int[] { -22, -11, 0, 11 };
	private static final int[] UR3 = new int[] { -33, -22, -11, 0 };

	private static final int[] R0 = new int[] { 0, 1, 2, 3 };
	private static final int[] R1 = new int[] { -1, 0, 1, 2 };
	private static final int[] R2 = new int[] { -2, -1, 0, 1 };
	private static final int[] R3 = new int[] { -3, -2, -1, 0 };

	private static final int[][] C00 = new int[][] { U0, UR0, R0 };
	private static final int[][] C01 = new int[][] { U0, UR0, R0, R1 };
	private static final int[][] C02 = new int[][] { U0, UR0, R0, R1, R2 };
	private static final int[][] C03 = new int[][] { UL0, U0, UR0, R0, R1, R2, R3 };
	private static final int[][] C04 = new int[][] { UL0, U0, R1, R2, R3 };
	private static final int[][] C05 = new int[][] { UL0, U0, R2, R3 };
	private static final int[][] C06 = new int[][] { UL0, U0, R3 };

	private static final int[][] C10 = new int[][] { U0, U1, UR0, R0 };
	private static final int[][] C11 = new int[][] { U0, U1, UR0, UR1, R0, R1 };
	private static final int[][] C12 = new int[][] { UL1, U0, U1, UR0, UR1, R0, R1, R2 };
	private static final int[][] C13 = new int[][] { UL0, UL1, U0, U1, UR0, UR1, R0, R1, R2, R3 };
	private static final int[][] C14 = new int[][] { UL0, UL1, U0, U1, UR1, R1, R2, R3 };
	private static final int[][] C15 = new int[][] { UL0, UL1, U0, U1, R2, R3 };
	private static final int[][] C16 = new int[][] { UL0, U0, U1, R3 };

	private static final int[][] C20 = new int[][] { U0, U1, U2, UR0, R0 };
	private static final int[][] C21 = new int[][] { UL2, U0, U1, U2, UR0, UR1, R0, R1 };
	private static final int[][] C22 = new int[][] { UL1, UL2, U0, U1, U2, UR0, UR1, UR2, R0, R1, R2 };
	private static final int[][] C23 = new int[][] { UL0, UL1, UL2, U0, U1, U2, UR0, UR1, UR2, R0, R1, R2, R3 };
	private static final int[][] C24 = new int[][] { UL0, UL1, UL2, U0, U1, U2, UR1, UR2, R1, R2, R3 };
	private static final int[][] C25 = new int[][] { UL0, UL1, U0, U1, U2, UR2, R2, R3 };
	private static final int[][] C26 = new int[][] { UL0, U0, U1, U2, R3 };

	private static final int[][] C30 = new int[][] { UL3, U0, U1, U2, U3, UR0, R0 };
	private static final int[][] C31 = new int[][] { UL2, UL3, U0, U1, U2, U3, UR0, UR1, R0, R1 };
	private static final int[][] C32 = new int[][] { UL1, UL2, UL3, U0, U1, U2, U3, UR0, UR1, UR2, R0, R1, R2 };
	private static final int[][] C33 = new int[][] { UL0, UL1, UL2, UL3, U0, U1, U2, U3, UR0, UR1, UR2, UR3, R0, R1, R2, R3 };
	private static final int[][] C34 = new int[][] { UL0, UL1, UL2, U0, U1, U2, U3, UR1, UR2, UR3, R1, R2, R3 };
	private static final int[][] C35 = new int[][] { UL0, UL1, U0, U1, U2, U3, UR2, UR3, R2, R3 };
	private static final int[][] C36 = new int[][] { UL0, U0, U1, U2, U3, UR3, R3 };

	private static final int[][] C40 = new int[][] { UL3, U1, U2, U3, R0 };
	private static final int[][] C41 = new int[][] { UL2, UL3, U1, U2, U3, UR1, R0, R1 };
	private static final int[][] C42 = new int[][] { UL1, UL2, UL3, U1, U2, U3, UR1, UR2, R0, R1, R2 };
	private static final int[][] C43 = new int[][] { UL1, UL2, UL3, U1, U2, U3, UR1, UR2, UR3, R0, R1, R2, R3 };
	private static final int[][] C44 = new int[][] { UL1, UL2, U1, U2, U3, UR1, UR2, UR3, R1, R2, R3 };
	private static final int[][] C45 = new int[][] { UL1, U1, U2, U3, UR2, UR3, R2, R3 };
	private static final int[][] C46 = new int[][] { U1, U2, U3, UR3, R3 };

	private static final int[][] C50 = new int[][] { UL3, U2, U3, R0 };
	private static final int[][] C51 = new int[][] { UL2, UL3, U2, U3, R0, R1 };
	private static final int[][] C52 = new int[][] { UL2, UL3, U2, U3, UR2, R0, R1, R2 };
	private static final int[][] C53 = new int[][] { UL2, UL3, U2, U3, UR2, UR3, R0, R1, R2, R3 };
	private static final int[][] C54 = new int[][] { UL2, U2, U3, UR2, UR3, R1, R2, R3 };
	private static final int[][] C55 = new int[][] { U2, U3, UR2, UR3, R2, R3 };
	private static final int[][] C56 = new int[][] { U2, U3, UR3, R3 };

	private static final int[][] C60 = new int[][] { UL3, U3, R0 };
	private static final int[][] C61 = new int[][] { UL3, U3, R0, R1 };
	private static final int[][] C62 = new int[][] { UL3, U3, R0, R1, R2 };
	private static final int[][] C63 = new int[][] { UL3, U3, UR3, R0, R1, R2, R3 };
	private static final int[][] C64 = new int[][] { U3, UR3, R1, R2, R3 };
	private static final int[][] C65 = new int[][] { U3, UR3, R2, R3 };
	private static final int[][] C66 = new int[][] { U3, UR3, R3 };

	private static final int[][][] CHAIN = new int[][][] {
			C00, C01, C02, C03, C04, C05, C06,
			C10, C11, C12, C13, C14, C15, C16,
			C20, C21, C22, C23, C24, C25, C26,
			C30, C31, C32, C33, C34, C35, C36,
			C40, C41, C42, C43, C44, C45, C46,
			C50, C51, C52, C53, C54, C55, C56,
			C60, C61, C62, C63, C64, C65, C66
	};

	private static final int VAL4 = 1000000000;
	private static final int VAL3 = 1000000;
	private static final int VAL2 = 1000;
	private static final int VAL1 = 10;
	private static final int VAL0 = 0;

	private static final int[] VAL = new int[] { VAL0, VAL1, VAL2, VAL3, VAL4 };

	private static byte winner(int base, int[] chain) throws Exception {
		int white = 0;
		int black = 0;
		for (int f = 0; f < 4; f++) {
			byte stone = board[base + chain[f]];
			if (stone == WHITE)
				white++;
			else if (stone == BLACK)
				black++;
			else if (stone != SPACE)
				throw new Exception();
		}
		if (white == 4)
			return WHITE;
		if (black == 4)
			return BLACK;
		return SPACE;
	}

	private static byte winner(int row, int column) throws Exception {
		int base = index(row, column);
		int[][] chains = CHAIN[row * COLUMNS + column];
		for (int c = 0; c < chains.length; c++) {
			byte winner = winner(base, chains[c]);
			if (winner != SPACE)
				return winner;
		}
		return SPACE;
	}

	private static byte winner(int column) throws Exception {
		byte winner = winner(top[column] - 1, column);
		if (winner != SPACE)
			return winner;
		boolean allColumnsFull = true;
		for (int c = 0; c < COLUMNS; c++)
			allColumnsFull &= top[c] >= ROWS;
		return allColumnsFull ? FRAME : SPACE;
	}

	private static int value(byte color, int base, int[] chain) throws Exception {
		int num = 0;
		for (int f = 0; f < 4; f++) {
			byte stone = board[base + chain[f]];
			if (stone == color)
				num++;
			else if (stone == WHITE || stone == BLACK)
				return VAL0;
			else if (stone != SPACE)
				throw new Exception();
		}
		return VAL[num];
	}

	private static int value(byte color, int base, int[][] chains) throws Exception {
		int value = 0;
		for (int c = 0; c < chains.length; c++) {
			int chainValue = value(color, base, chains[c]);
			if (chainValue > value)
				value = chainValue;
		}
		return value;
	}

	private static int value(int row, int column) throws Exception {
		int base = index(row, column);
		int[][] chains = CHAIN[row * COLUMNS + column];
		return value(WHITE, base, chains) - value(BLACK, base, chains);
	}

	private static int value() throws Exception {
		int value = 0;
		for (int c = 0; c < COLUMNS; c++) {
			for (int r = top[c]; r < ROWS; r++) {
				value += value(r, c);
			}
		}
		return value;
	}

	// Search

	private static int white(int alpha, int beta) throws Exception {
		return 0;
	}

	private static int black(int alpha, int beta) throws Exception {
		return 0;
	}

}
