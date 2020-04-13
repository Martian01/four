package com.mr.four;

import java.util.List;
import java.util.Scanner;

public class Main {

	public static final Scanner in = new Scanner(System.in);

	private static Game game;

	public static void main(String[] args) {
		game = new Game();
		for (String arg : args)
			if ("debug".equals(arg)) {
				game.log = new Log();
				break;
			}
		System.out.println("\nGreetings, Professor Falken. Shall we play a game?");
		System.out.print("\nWhat is your level (0, 1, 2, 3 ...)? ");
		game.maxLevel = in.nextByte();
		game.init();
		if (game.log != null) {
			game.log.openFile();
			game.log.openNode("match");
		}
		loop();
		if (game.log != null)
			game.log.closeFile();
	}

	// Game Loop

	private static void loop() {
		byte color = Game.WHITE;
		byte winner;
		do {
			game.printBoard();
			byte column = color == Game.WHITE ? strategyUser(color) : strategySearch(color);
			game.drop(column, color);
			if (game.log != null) game.log.logNode("drop", "column" , Byte.toString(column));
			winner = game.winner(column);
			color = Game.opposite(color);
			//game.printValue();
		}
		while (winner == Game.SPACE);
		if (game.log != null) game.log.logNode("result", "winner" , Byte.toString(winner));
		game.printBoard();
		switch (winner) {
			case Game.WHITE: System.out.println("You win.\n"); break;
			case Game.BLACK: System.out.println("You lose.\n"); break;
			default: System.out.println("It's a draw.\n");
		}
	}

	private static byte strategyUser(byte color) {
		if (game.log != null) game.log.logNode("user");
		for (;;) {
			System.out.print("Your move for " + (color == Game.WHITE ? "white: " : "black: "));
			byte column = (byte) in.nextByte() ;
			if (game.isOption(column))
				return column;
			System.out.println("Illegal move. Try again.");
		}
	}

	private static byte strategyRandom(byte color) {
		if (game.log != null) game.log.logNode("random");
		List<Byte> options = game.getOptions();
		double r = Math.random();
		byte column = options.get((int) (r * r * options.size()));
		System.out.println("Random move for " + (color == Game.WHITE ? "white: " : "black: ") + column);
		return column;
	}

	private static byte strategySearch(byte color) {
		if (game.log != null) game.log.openNode("search");
		byte column = game.search(color);
		if (game.log != null) game.log.closeNode();
		System.out.println("Search result for " + (color == Game.WHITE ? "white: " : "black: ") + column);
		return column;
	}

}
