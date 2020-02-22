package com.mr.four;

public class Main {

	public static void main(String[] args) {
		System.out.println("\nGreetings, Professor Falken. Shall we play a game?");
		System.out.print("\nWhat is your level (0, 1, 2, 3 ...)? ");
		byte level = (byte) Game.in.nextByte();
		Log log = null;
		for (String arg : args)
			if ("debug".equals(arg)) {
				log = new Log();
				break;
			}
		new Game(level, log).start();
	}

}
