package com.mr.four;

import java.util.*;

public class Game {

	// Static board definition

	// Note: we prefer 7x7 to the more common 6x7 as it offers better Zugzwang opportunities

	public static final byte SPACE = 0;
	public static final byte WHITE = 1;
	public static final byte BLACK = 2;

	public static final byte RUNNING = 0;
	public static final byte DRAW = 3;

	public static byte opposite(byte color) {
		return (byte) (3 - color);
	}

	private static final byte[] INITIAL_BOARD = new byte[] {
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
			SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
	};

	private static final byte[] ZERO_COLUMNS = new byte[] { 0, 0, 0, 0, 0, 0, 0 };

	private static final byte ROWS = 7;
	private static final byte COLUMNS = 7;

	// Static definitions for chains, specific to a 7x7 board

	private static final byte[] UL0 = new byte[] { 0, 6, 12, 18 };
	private static final byte[] UL1 = new byte[] { -6, 0, 6, 12 };
	private static final byte[] UL2 = new byte[] { -12, -6, 0, 6 };
	private static final byte[] UL3 = new byte[] { -18, -12, -6, 0 };

	private static final byte[] U0 = new byte[] { 0, 7, 14, 21 };
	private static final byte[] U1 = new byte[] { -7, 0, 7, 14 };
	private static final byte[] U2 = new byte[] { -14, -7, 0, 7 };
	private static final byte[] U3 = new byte[] { -21, -14, -7, 0 };

	private static final byte[] UR0 = new byte[] { 0, 8, 16, 24 };
	private static final byte[] UR1 = new byte[] { -8, 0, 8, 16 };
	private static final byte[] UR2 = new byte[] { -16, -8, 0, 8 };
	private static final byte[] UR3 = new byte[] { -24, -16, -8, 0 };

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

	// Static definitions for valuation

	private static final int VAL4 = 0x10000000;
	private static final int VAL3 = 0x100000;
	private static final int VAL2 = 0x1000;
	private static final int VAL1 = 0x10;
	private static final int VAL0 = 0x0;

	private static final int[] VAL = new int[] { VAL0, VAL1, VAL2, VAL3, VAL4 };

	private static final int[] WINNER_VAL = new int[] { 0, VAL4, -VAL4, 0};

	// Static debug support for console version

	private static String dbg(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < a.length; i++) {
			sb.append(a[i]);
			if (i < a.length - 1)
				sb.append(",");
		}
		return sb.toString();
	}

	private static String dbx(int[] a) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < a.length; i++) {
			sb.append(Integer.toHexString(a[i]));
			if (i < a.length - 1)
				sb.append(",");
		}
		return sb.toString();
	}

	private static String dbg(byte i) {
		return Byte.toString(i);
	}

	private static String dbx(int i) {
		return Integer.toHexString(i);
	}

	// Instance variables and methods for console version

	public Log log;

	public void printBoard() {
		System.out.println("\n  0 1 2 3 4 5 6   ");
		for (byte r = ROWS - 1; r >= 0; r--) {
			StringBuilder line = new StringBuilder("| ");
			for (byte c = 0; c < COLUMNS; c++) {
				switch (board[index(r, c)]) {
					case WHITE: line.append("o "); break;
					case BLACK: line.append("x "); break;
					default: line.append("  ");
				}
			}
			line.append("|");
			System.out.println(line.toString());
		}
		System.out.println("----------------- \n");
	}

	public void printValue() throws Exception {
		System.out.println("\nValue: " + dbx(value()) + "\n");
	}

	// Instance variables and methods

	public byte maxLevel;

	public void init() {
		System.arraycopy(INITIAL_BOARD, 0, board, 0, INITIAL_BOARD.length);
		System.arraycopy(ZERO_COLUMNS, 0, top, 0, ZERO_COLUMNS.length);
	}

	public byte search(byte color) throws Exception {
		long result = search(color, Integer.MIN_VALUE, Integer.MAX_VALUE, (byte) 1);
		return (byte) (result & 0xF);
	}

	// Board management

	private int index(byte row, byte column) {
		return COLUMNS * row + column;
	}

	private byte[] board = new byte[INITIAL_BOARD.length];

	private byte[] top = new byte[ZERO_COLUMNS.length];

	public void drop(byte column, byte color) {
		board[index(top[column]++, column)] = color;
	}

	public void revert(byte column) {
		board[index(--top[column], column)] = SPACE;
	}

	public byte getTop(byte column) {
		return column >= 0 && column < COLUMNS ? top[column] : 0;
	}

	public boolean isOption(byte column) {
		return column >= 0 && column < COLUMNS && top[column] < ROWS;
	}

	private final byte[] COLUMN_SEQUENCE = new byte[] { 3, 2, 4, 1, 5, 0, 6 };

	public List<Byte> getOptions() throws Exception {
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

	// Board valuation

	private byte winner(int base, byte[] chain) throws Exception {
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
		return RUNNING;
	}

	public byte winner(byte row, byte column) throws Exception {
		int base = index(row, column);
		byte[][] chains = CHAIN[row * COLUMNS + column];
		for (byte c = 0; c < chains.length; c++) {
			byte winner = winner(base, chains[c]);
			if (winner != RUNNING)
				return winner;
		}
		return RUNNING;
	}

	public byte winner(byte column) throws Exception {
		byte winner = winner((byte) (top[column] - 1), column);
		if (winner != RUNNING)
			return winner;
		boolean allColumnsFull = true;
		for (byte c = 0; c < COLUMNS; c++)
			allColumnsFull &= top[c] >= ROWS;
		return allColumnsFull ? DRAW : RUNNING;
	}

	private int value(byte color, int base, byte[] chain) throws Exception {
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

	private int value(byte color, int base, byte[][] chains) throws Exception {
		int value = 0;
		for (byte c = 0; c < chains.length; c++) {
			int chainValue = value(color, base, chains[c]);
			if (chainValue > value)
				value = chainValue;
		}
		return value;
	}

	private int value(byte row, byte column) throws Exception {
		int base = index(row, column);
		byte[][] chains = CHAIN[row * COLUMNS + column];
		return value(WHITE, base, chains) - value(BLACK, base, chains);
	}

	private int value() throws Exception {
		int value = 0;
		for (byte c = 0; c < COLUMNS; c++)
			for (byte r = top[c]; r < ROWS; r++)
				value += value(r, c);
		return value;
	}

	private int value(byte row, byte column, byte[] lowestWhite, byte[] lowestBlack) throws Exception {
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

	private int value(byte[] lowestWhite, byte[] lowestBlack) throws Exception {
		int value = 0;
		for (byte c = 0; c < COLUMNS; c++)
			for (byte r = top[c]; r < ROWS; r++)
				value += value(r, c, lowestWhite, lowestBlack);
		return value;
	}

	private int winValue(byte winner, byte level) {
		int value = WINNER_VAL[winner];
		return value < 0 ? value + (level << 4) : (value > 0 ? value - (level << 4) : value);
	}

	// Search

	private int search(byte color, int alpha, int beta, byte level) throws Exception {
		if (log != null) log.logNode(color, level, "alpha", dbx(alpha), "beta", dbx(beta));
		byte[] lowestWhite = new byte[] { -1, -1, -1, -1, -1, -1, -1 };
		byte[] lowestBlack = new byte[] { -1, -1, -1, -1, -1, -1, -1 };
		int value = value(lowestWhite, lowestBlack);
		if (log != null) log.logNode(color, level, "val", dbx(value), "lw", dbg(lowestWhite), "lb", dbg(lowestBlack));
		// build a sorted work list of moves
		int[] moves = new int[] { 0x00, 0x11, 0x22, 0x33, 0x24, 0x15, 0x06}; // low nybble equals column index
		for (byte c = 0; c < COLUMNS; c++) {
			byte r = color == WHITE ? lowestWhite[c] : lowestBlack[c];
			if (r >= 0)
				moves[c] |= r == top[c] ? 0x2000 : 0x200;
			r = color == WHITE ? lowestBlack[c] : lowestWhite[c];
			if (r >= 0)
				moves[c] |= r == top[c] ? 0x1000 : 0x100;
		}
		Arrays.sort(moves);
		if (log != null) log.logNode(color, level, "moves", dbx(moves));
		byte bestColumn = 0xF;
		int bestValue = color == WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
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
						if (log != null) log.logNode(color, level, "mateVal", dbx(winValue(color, level)), "mateCol", dbg(column));
						return winValue(color, level) | column;
					}
					// check if this is a leaf of the search tree
					if (level > maxLevel && move < 0x1000) { // no hidden mates
						if (log != null) log.logNode(color, level, "leafVal", dbx(value), "leafCol", dbg(column));
						return value | column;
					}
				}
				if (level <= maxLevel || move >= 0x1000) { // normal search or quiescence search
					drop(column, color);
					byte winner = winner(column);
					if (winner != RUNNING) {
						revert(column);
						if (log != null) log.logNode(color, level, "winVal", dbx(winValue(winner, level)), "winCol", dbg(column));
						return winValue(winner, level) | column;
					}
					if (log != null) log.openNode(color, level, "drop", dbg(column), "alpha", dbx(alpha), "beta", dbx(beta));
					value = search(opposite(color) , alpha, beta, (byte) (level + 1)) & 0xFFFFFFF0;
					revert(column);
					if (color == WHITE) {
						if (value > bestValue) {
							bestValue = value;
							bestColumn = column;
						}
						if (value > alpha)
							alpha = value;
					} else {
						if (value < bestValue) {
							bestValue = value;
							bestColumn = column;
						}
						if (value < beta)
							beta = value;
					}
					if (log != null) log.logNode(color, level, "revert", dbg(column), "value", dbx(value), "bestValue", dbx(bestValue), "bestCol", dbg(bestColumn), "alpha", dbx(alpha), "beta", dbx(beta));
					if (alpha >= beta) {
						if (log != null) log.logNode(color, level, "pruning", "true");
						break;
					}
				}
			}
		}
		if (log != null) log.logNode(color, level, "bestVal", dbx(bestValue), "bstCol", dbg(bestColumn));
		if (bestColumn == 0xF)
			throw new Exception();
		return bestValue | bestColumn;
	}

}
