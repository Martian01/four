package com.mr.four;

import java.util.List;
import java.util.Scanner;

public class Main {

	public static final Scanner in = new Scanner(System.in);

	private static Log log;
	private static Game game;

	public static void main(String[] args) {
		for (String arg : args)
			if ("debug".equals(arg)) {
				log = new Log();
				break;
			}
		game = new Game();
		game.log = log;
		System.out.println("\nGreetings, Professor Falken. Shall we play a game?");
		System.out.print("\nWhat is your level (0, 1, 2, 3 ...)? ");
		game.maxLevel = in.nextByte();
		game.init();
		try {
			if (log != null) {
				log.openFile();
				log.openNode("match");
			}
			loop();
			if (log != null)
				log.closeFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Game Loop

	private static void loop() throws Exception {
		byte color = Game.WHITE;
		byte winner;
		do {
			game.printBoard();
			byte column = color == Game.WHITE ? strategyUser(color) : strategySearch(color);
			game.drop(column, color);
			if (log != null) log.logNode("drop", "column" , Byte.toString(column));
			winner = game.winner(column);
			color = Game.opposite(color);
		}
		while (winner == Game.SPACE);
		if (log != null) log.logNode("result", "winner" , Byte.toString(winner));
		game.printBoard();
		switch (winner) {
			case Game.WHITE: System.out.println("You win.\n"); break;
			case Game.BLACK: System.out.println("You lose.\n"); break;
			default: System.out.println("It's a draw.\n");
		}
	}

	private static byte strategyUser(byte color) throws Exception {
		if (log != null) log.logNode("user");
		for (;;) {
			System.out.print("Your move for " + (color == Game.WHITE ? "white: " : "black: "));
			byte column = (byte) in.nextByte() ;
			if (game.isOption(column))
				return column;
			System.out.println("Illegal move. Try again.");
		}
	}

	private static byte strategyRandom(byte color) throws Exception {
		if (log != null) log.logNode("random");
		List<Byte> options = game.getOptions();
		double r = Math.random();
		byte column = options.get((int) (r * r * options.size()));
		System.out.println("Random move for " + (color == Game.WHITE ? "white: " : "black: ") + column);
		return column;
	}

	private static byte strategySearch(byte color) throws Exception {
		if (log != null) log.openNode("search");
		byte column = game.search(color);
		if (log != null) log.closeNode();
		System.out.println("Search result for " + (color == Game.WHITE ? "white: " : "black: ") + column);
		return column;
	}

}
