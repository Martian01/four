package com.mr.four;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

	private static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
		System.out.println("\nGreetings, Professor Falken. Shall we play a game?");
		System.out.print("\nWhat is your level (0 = poor, 1, 2, 3 = stronger)? ");
		byte b = (byte) in.nextByte();
		DEBUG = (b & 0xF0) != 0;
		LEVEL = (byte) (b & 0x0F);
	    start();
    }

	// Board Definition

	// Note: we prefer 7x7 to the more common 6x7 as it offers better Zugzwang opportunities

	private static final byte SPACE = 0;
	private static final byte WHITE = 1;
	private static final byte BLACK = 2;
	private static final byte FRAME = 3;

	private static final byte[] INITIAL_BOARD = new byte[] {
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

	private static final byte[] ZERO_COLUMNS = new byte[] { 0, 0, 0, 0, 0, 0, 0 };

	private static final byte ROWS = 7;
	private static final byte COLUMNS = 7;

	private static int index(byte row, byte column) {
		return (COLUMNS + 3) * (row + 3) + column;
	}

	private static byte[] board = new byte[INITIAL_BOARD.length];

	private static byte[] top = new byte[ZERO_COLUMNS.length];

	private static void drop(byte column, byte color) {
		board[index(top[column]++, column)] = color;
	}

	private static void revert(byte column) {
		board[index(--top[column], column)] = SPACE;
	}

	private static boolean isOption(byte column) {
		return column >= 0 && column < COLUMNS && top[column] < ROWS;
	}

	private static final byte[] COLUMN_SEQUENCE = new byte[] { 3, 2, 4, 1, 5, 0, 6 };

	private static List<Byte> getOptions() throws Exception {
		ArrayList<Byte> options = new ArrayList<>(COLUMNS);
		for (byte i = 0; i < COLUMNS; i++) {
			byte c = COLUMN_SEQUENCE[i];
			if (top[c] < ROWS)
				options.add(c);
		}
		if (options.isEmpty())
			throw new Exception();
		return options;
	}

	// Game Loop

	private static void start() {
		System.arraycopy(INITIAL_BOARD, 0, board, 0, INITIAL_BOARD.length);
		System.arraycopy(ZERO_COLUMNS, 0, top, 0, ZERO_COLUMNS.length);
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
			byte column = player == WHITE ? strategyUser(player) : strategySearch(player);
			drop(column, player);
			winner = winner(column);
			player = (byte) (3 - player);
		}
		while (winner == SPACE);
		printBoard();
		switch (winner) {
			case WHITE: System.out.println("You win.\n"); break;
			case BLACK: System.out.println("You lose.\n"); break;
			default: System.out.println("It's a draw.\n");
		}
	}

	private static byte strategyUser(byte color) {
		for (;;) {
			byte column = getUserInput(color);
			if (isOption(column))
				return column;
			System.out.println("Illegal move. Try again.");
		}
	}

	private static byte strategyRandom(byte color) throws Exception {
		List<Byte> options = getOptions();
		double r = Math.random();
		return options.get((int) (r * r * options.size()));
	}

	private static byte LEVEL = 3;

	private static byte strategySearch(byte color) throws Exception {
		long result = color == WHITE ? white(Integer.MIN_VALUE, Integer.MAX_VALUE, LEVEL) : black(Integer.MIN_VALUE, Integer.MAX_VALUE, LEVEL);
		return (byte) (result & 0xF);
	}

	// User Interaction

	private static void printBoard() {
		System.out.println("\n  0 1 2 3 4 5 6   ");
		for (byte r = ROWS - 1; r >= 0; r--) {
			StringBuilder line = new StringBuilder();
			int base = index(r, (byte) -1);
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

	private static byte getUserInput(byte color) {
		System.out.print("Your move for " + (color == WHITE ? "white: " : "black: "));
		return (byte) in.nextByte() ;
	}

	// Valuation, specific to a 7x7 board

	private static final byte[] UL0 = new byte[] { 0, 9, 18, 27 };
	private static final byte[] UL1 = new byte[] { -9, 0, 9, 18 };
	private static final byte[] UL2 = new byte[] { -18, -9, 0, 9 };
	private static final byte[] UL3 = new byte[] { -27, -18, -9, 0 };

	private static final byte[] U0 = new byte[] { 0, 10, 20, 30 };
	private static final byte[] U1 = new byte[] { -10, 0, 10, 20 };
	private static final byte[] U2 = new byte[] { -20, -10, 0, 10 };
	private static final byte[] U3 = new byte[] { -30, -20, -10, 0 };

	private static final byte[] UR0 = new byte[] { 0, 11, 22, 33 };
	private static final byte[] UR1 = new byte[] { -11, 0, 11, 22 };
	private static final byte[] UR2 = new byte[] { -22, -11, 0, 11 };
	private static final byte[] UR3 = new byte[] { -33, -22, -11, 0 };

	private static final byte[] R0 = new byte[] { 0, 1, 2, 3 };
	private static final byte[] R1 = new byte[] { -1, 0, 1, 2 };
	private static final byte[] R2 = new byte[] { -2, -1, 0, 1 };
	private static final byte[] R3 = new byte[] { -3, -2, -1, 0 };

	private static final byte[][] C00 = new byte[][] { U0, UR0, R0 };
	private static final byte[][] C01 = new byte[][] { U0, UR0, R0, R1 };
	private static final byte[][] C02 = new byte[][] { U0, UR0, R0, R1, R2 };
	private static final byte[][] C03 = new byte[][] { UL0, U0, UR0, R0, R1, R2, R3 };
	private static final byte[][] C04 = new byte[][] { UL0, U0, R1, R2, R3 };
	private static final byte[][] C05 = new byte[][] { UL0, U0, R2, R3 };
	private static final byte[][] C06 = new byte[][] { UL0, U0, R3 };

	private static final byte[][] C10 = new byte[][] { U0, U1, UR0, R0 };
	private static final byte[][] C11 = new byte[][] { U0, U1, UR0, UR1, R0, R1 };
	private static final byte[][] C12 = new byte[][] { UL1, U0, U1, UR0, UR1, R0, R1, R2 };
	private static final byte[][] C13 = new byte[][] { UL0, UL1, U0, U1, UR0, UR1, R0, R1, R2, R3 };
	private static final byte[][] C14 = new byte[][] { UL0, UL1, U0, U1, UR1, R1, R2, R3 };
	private static final byte[][] C15 = new byte[][] { UL0, UL1, U0, U1, R2, R3 };
	private static final byte[][] C16 = new byte[][] { UL0, U0, U1, R3 };

	private static final byte[][] C20 = new byte[][] { U0, U1, U2, UR0, R0 };
	private static final byte[][] C21 = new byte[][] { UL2, U0, U1, U2, UR0, UR1, R0, R1 };
	private static final byte[][] C22 = new byte[][] { UL1, UL2, U0, U1, U2, UR0, UR1, UR2, R0, R1, R2 };
	private static final byte[][] C23 = new byte[][] { UL0, UL1, UL2, U0, U1, U2, UR0, UR1, UR2, R0, R1, R2, R3 };
	private static final byte[][] C24 = new byte[][] { UL0, UL1, UL2, U0, U1, U2, UR1, UR2, R1, R2, R3 };
	private static final byte[][] C25 = new byte[][] { UL0, UL1, U0, U1, U2, UR2, R2, R3 };
	private static final byte[][] C26 = new byte[][] { UL0, U0, U1, U2, R3 };

	private static final byte[][] C30 = new byte[][] { UL3, U0, U1, U2, U3, UR0, R0 };
	private static final byte[][] C31 = new byte[][] { UL2, UL3, U0, U1, U2, U3, UR0, UR1, R0, R1 };
	private static final byte[][] C32 = new byte[][] { UL1, UL2, UL3, U0, U1, U2, U3, UR0, UR1, UR2, R0, R1, R2 };
	private static final byte[][] C33 = new byte[][] { UL0, UL1, UL2, UL3, U0, U1, U2, U3, UR0, UR1, UR2, UR3, R0, R1, R2, R3 };
	private static final byte[][] C34 = new byte[][] { UL0, UL1, UL2, U0, U1, U2, U3, UR1, UR2, UR3, R1, R2, R3 };
	private static final byte[][] C35 = new byte[][] { UL0, UL1, U0, U1, U2, U3, UR2, UR3, R2, R3 };
	private static final byte[][] C36 = new byte[][] { UL0, U0, U1, U2, U3, UR3, R3 };

	private static final byte[][] C40 = new byte[][] { UL3, U1, U2, U3, R0 };
	private static final byte[][] C41 = new byte[][] { UL2, UL3, U1, U2, U3, UR1, R0, R1 };
	private static final byte[][] C42 = new byte[][] { UL1, UL2, UL3, U1, U2, U3, UR1, UR2, R0, R1, R2 };
	private static final byte[][] C43 = new byte[][] { UL1, UL2, UL3, U1, U2, U3, UR1, UR2, UR3, R0, R1, R2, R3 };
	private static final byte[][] C44 = new byte[][] { UL1, UL2, U1, U2, U3, UR1, UR2, UR3, R1, R2, R3 };
	private static final byte[][] C45 = new byte[][] { UL1, U1, U2, U3, UR2, UR3, R2, R3 };
	private static final byte[][] C46 = new byte[][] { U1, U2, U3, UR3, R3 };

	private static final byte[][] C50 = new byte[][] { UL3, U2, U3, R0 };
	private static final byte[][] C51 = new byte[][] { UL2, UL3, U2, U3, R0, R1 };
	private static final byte[][] C52 = new byte[][] { UL2, UL3, U2, U3, UR2, R0, R1, R2 };
	private static final byte[][] C53 = new byte[][] { UL2, UL3, U2, U3, UR2, UR3, R0, R1, R2, R3 };
	private static final byte[][] C54 = new byte[][] { UL2, U2, U3, UR2, UR3, R1, R2, R3 };
	private static final byte[][] C55 = new byte[][] { U2, U3, UR2, UR3, R2, R3 };
	private static final byte[][] C56 = new byte[][] { U2, U3, UR3, R3 };

	private static final byte[][] C60 = new byte[][] { UL3, U3, R0 };
	private static final byte[][] C61 = new byte[][] { UL3, U3, R0, R1 };
	private static final byte[][] C62 = new byte[][] { UL3, U3, R0, R1, R2 };
	private static final byte[][] C63 = new byte[][] { UL3, U3, UR3, R0, R1, R2, R3 };
	private static final byte[][] C64 = new byte[][] { U3, UR3, R1, R2, R3 };
	private static final byte[][] C65 = new byte[][] { U3, UR3, R2, R3 };
	private static final byte[][] C66 = new byte[][] { U3, UR3, R3 };

	private static final byte[][][] CHAIN = new byte[][][] {
			C00, C01, C02, C03, C04, C05, C06,
			C10, C11, C12, C13, C14, C15, C16,
			C20, C21, C22, C23, C24, C25, C26,
			C30, C31, C32, C33, C34, C35, C36,
			C40, C41, C42, C43, C44, C45, C46,
			C50, C51, C52, C53, C54, C55, C56,
			C60, C61, C62, C63, C64, C65, C66
	};

	private static final int VAL4 = 0x10000000;
	private static final int VAL3 = 0x100000;
	private static final int VAL2 = 0x1000;
	private static final int VAL1 = 0x10;
	private static final int VAL0 = 0x0;

	private static final int[] VAL = new int[] { VAL0, VAL1, VAL2, VAL3, VAL4 };

	private static byte winner(int base, byte[] chain) throws Exception {
		byte white = 0;
		byte black = 0;
		for (byte f = 0; f < 4; f++) {
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

	private static byte winner(byte row, byte column) throws Exception {
		int base = index(row, column);
		byte[][] chains = CHAIN[row * COLUMNS + column];
		for (byte c = 0; c < chains.length; c++) {
			byte winner = winner(base, chains[c]);
			if (winner != SPACE)
				return winner;
		}
		return SPACE;
	}

	private static byte winner(byte column) throws Exception {
		byte winner = winner((byte) (top[column] - 1), column);
		if (winner != SPACE)
			return winner;
		boolean allColumnsFull = true;
		for (byte c = 0; c < COLUMNS; c++)
			allColumnsFull &= top[c] >= ROWS;
		return allColumnsFull ? FRAME : SPACE;
	}

	private static final int[] WINNER_VAL = new int[] { 0, VAL4, -VAL4, 0};

	private static int value(byte color, int base, byte[] chain) throws Exception {
		byte num = 0;
		for (byte f = 0; f < 4; f++) {
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

	private static int value(byte color, int base, byte[][] chains) throws Exception {
		int value = 0;
		for (byte c = 0; c < chains.length; c++) {
			int chainValue = value(color, base, chains[c]);
			if (chainValue > value)
				value = chainValue;
		}
		return value;
	}

	private static int value(byte row, byte column) throws Exception {
		int base = index(row, column);
		byte[][] chains = CHAIN[row * COLUMNS + column];
		return value(WHITE, base, chains) - value(BLACK, base, chains);
	}

	private static int value() throws Exception {
		int value = 0;
		for (byte c = 0; c < COLUMNS; c++)
			for (byte r = top[c]; r < ROWS; r++)
				value += value(r, c);
		return value;
	}

	private static int value(byte row, byte column, byte[] lowestWhite, byte[] lowestBlack) throws Exception {
		int base = index(row, column);
		byte[][] chains = CHAIN[row * COLUMNS + column];
		int valueWhite = value(WHITE, base, chains);
		int valueBlack = value(BLACK, base, chains);
		if (valueWhite >= VAL3 && lowestWhite[column] < 0)
			lowestWhite[column] = row;
		if (valueBlack >= VAL3 && lowestBlack[column] < 0)
			lowestBlack[column] = row;
		return valueWhite - valueBlack;
	}

	private static int value(byte[] lowestWhite, byte[] lowestBlack) throws Exception {
		int value = 0;
		for (byte c = 0; c < COLUMNS; c++)
			for (byte r = top[c]; r < ROWS; r++)
				value += value(r, c, lowestWhite, lowestBlack);
		return value;
	}

	// Search

	private static boolean DEBUG = false;

	private static String dbg(byte[] a) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < a.length; i++) {
			sb.append(a[i]);
			sb.append(i == a.length - 1 ? "]" : ",");
		}
		return sb.toString();
	}

	private static String dbg(int[] a) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < a.length; i++) {
			sb.append(Integer.toHexString(a[i]));
			sb.append(i == a.length - 1 ? "]" : ",");
		}
		return sb.toString();
	}

	private static String dbg(int i) {
		return Integer.toHexString(i);
	}

	private static int white(int alpha, int beta, byte level) throws Exception {
		if (DEBUG) System.out.println("W" + level + " alpha=" + dbg(alpha) + " beta=" + dbg(beta));
		byte[] lowestWhite = new byte[] { -1, -1, -1, -1, -1, -1, -1 };
		byte[] lowestBlack = new byte[] { -1, -1, -1, -1, -1, -1, -1 };
		int value = value(lowestWhite, lowestBlack);
		if (DEBUG) System.out.println("W" + level + " val=" + dbg(value) + " lw=" + dbg(lowestWhite) + " lb=" + dbg(lowestBlack));
		// build a sorted work list of moves
		int[] moves = new int[] { 0x00, 0x11, 0x22, 0x33, 0x24, 0x15, 0x06}; // low nybble equals column index
		for (byte c = 0; c < COLUMNS; c++) {
			byte r = lowestWhite[c];
			if (r >= 0)
				moves[c] |= r == top[c] ? 0x2000 : 0x200;
			r = lowestBlack[c];
			if (r >= 0)
				moves[c] |= r == top[c] ? 0x1000 : 0x100;
		}
		Arrays.sort(moves);
		if (DEBUG) System.out.println("W" + level + " moves=" + dbg(moves));
		byte bestColumn = 0xF;
		int bestValue = Integer.MIN_VALUE;
		// go through all legal moves in the sorted list
		boolean notSeenHighestRatedLegalMove = true;
		for (byte i = COLUMNS - 1; i >= 0; i--) { // from highest to lowest rating
			int move = moves[i];
			byte column = (byte) (move & 0xF);
			if (top[column] < ROWS) { // legal move
				if (notSeenHighestRatedLegalMove) {
					notSeenHighestRatedLegalMove = false;
					// shortcut for mate situation
					if ((move & 0x2000) != 0) {
						if (DEBUG) System.out.println("W" + level + " mateVal=" + dbg(VAL4) + " mateCol=" + column);
						return VAL4 | column;
					}
					// check if this is a leaf of the search tree
					if (level <= 0 && move < 0x100) { // no hidden mates
						if (DEBUG) System.out.println("W" + level + " leafVal=" + dbg(value) + " leafCol=" + column);
						return value | column;
					}
				}
				if (level > 0 || move >= 0x100) { // quiescence search
					drop(column, WHITE);
					byte winner = winner(column);
					if (winner != SPACE) {
						revert(column);
						if (DEBUG) System.out.println("W" + level + " winVal=" + dbg(WINNER_VAL[winner]) + " winCol=" + column);
						return WINNER_VAL[winner] | column;
					}
					if (DEBUG) System.out.println("W" + level + " drop column=" + column + " alpha=" + dbg(alpha) + " beta=" + dbg(beta));
					value = black(alpha, beta, (byte) (level - 1)) & 0xFFFFFFF0;
					revert(column);
					if (value > bestValue) {
						bestValue = value;
						bestColumn = column;
					}
					if (value > alpha)
						alpha = value;
					if (alpha >= beta) {
						if (DEBUG) System.out.println("W" + level + " pruning");
						break;
					}
				}
			}
		}
		if (DEBUG) System.out.println("W" + level + " bestVal=" + dbg(bestValue) + " bstCol=" + bestColumn);
		if (bestColumn == 0xF)
			throw new Exception();
		return bestValue | bestColumn;
	}

	private static int black(int alpha, int beta, byte level) throws Exception {
		if (DEBUG) System.out.println("B" + level + " alpha=" + dbg(alpha) + " beta=" + dbg(beta));
		byte[] lowestWhite = new byte[] { -1, -1, -1, -1, -1, -1, -1 };
		byte[] lowestBlack = new byte[] { -1, -1, -1, -1, -1, -1, -1 };
		int value = value(lowestWhite, lowestBlack);
		if (DEBUG) System.out.println("B" + level + " val=" + dbg(value) + " lw=" + dbg(lowestWhite) + " lb=" + dbg(lowestBlack));
		// build a sorted work list of moves
		int[] moves = new int[] { 0x00, 0x11, 0x22, 0x33, 0x24, 0x15, 0x06}; // low nybble equals column index
		for (byte c = 0; c < COLUMNS; c++) {
			byte r = lowestBlack[c];
			if (r >= 0)
				moves[c] |= r == top[c] ? 0x2000 : 0x200;
			r = lowestWhite[c];
			if (r >= 0)
				moves[c] |= r == top[c] ? 0x1000 : 0x100;
		}
		Arrays.sort(moves);
		if (DEBUG) System.out.println("B" + level + " moves=" + dbg(moves));
		byte bestColumn = 0xF;
		int bestValue = Integer.MAX_VALUE;
		// go through all legal moves in the sorted list
		boolean notSeenHighestRatedLegalMove = true;
		for (byte i = COLUMNS - 1; i >= 0; i--) { // from highest to lowest rating
			int move = moves[i];
			byte column = (byte) (move & 0xF);
			if (top[column] < ROWS) { // legal move
				if (notSeenHighestRatedLegalMove) {
					notSeenHighestRatedLegalMove = false;
					// shortcut for mate situation
					if ((move & 0x2000) != 0) {
						if (DEBUG) System.out.println("B" + level + " mateVal=" + dbg(-VAL4) + " mateCol=" + column);
						return -VAL4 | column;
					}
					// check if this is a leaf of the search tree
					if (level <= 0 && move < 0x100) { // no hidden mates
						if (DEBUG) System.out.println("B" + level + " leafVal=" + dbg(value) + " leafCol=" + column);
						return value | column;
					}
				}
				if (level > 0 || move >= 0x100) { // quiescence search
					drop(column, BLACK);
					byte winner = winner(column);
					if (winner != SPACE) {
						revert(column);
						if (DEBUG) System.out.println("B" + level + " winVal=" + dbg(WINNER_VAL[winner]) + " winCol=" + column);
						return WINNER_VAL[winner] | column;
					}
					if (DEBUG) System.out.println("B" + level + " drop column=" + column + " alpha=" + dbg(alpha) + " beta=" + dbg(beta));
					value = white(alpha, beta, (byte) (level - 1)) & 0xFFFFFFF0;
					revert(column);
					if (value < bestValue) {
						bestValue = value;
						bestColumn = column;
					}
					if (value < beta)
						beta = value;
					if (alpha >= beta) {
						if (DEBUG) System.out.println("B" + level + " pruning");
						break;
					}
				}
			}
		}
		if (DEBUG) System.out.println("B" + level + " bestVal=" + dbg(bestValue) + " bstCol=" + bestColumn);
		if (bestColumn == 0xF)
			throw new Exception();
		return bestValue | bestColumn;
	}

}
