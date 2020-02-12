# Four In A Row

This is an implementation of a game engine in Java. The user interaction happens in the most simple way via system console.

## How to run it

You can simply load the project in your favourite Java IDE and hit the "Execute" button.

## Stand-alone Version

You can create a stand-alone version in the form of a jar file. I provided a script `build.sh` that will create a jar file, assuming the .class files have been created by the IntelliJ IDEA IDE. Your mileage might vary. A ready-made jar file is included in the git repository. You can execute it like this:

	java -jar four.jar

## Debugging

Part of the purpose of this project is to gain a better understanding of the underlying minimax search. Since the trace of the hierarchical search tree gets very long and reading it becomes almost impossible, a solution was needed to browse the search tree in a hierarchical way.

You create the trace by starting the game with the command line option `debug`, for instance:

	java -jar four.jar debug

The game will create a log file named `debug001.xml` or similar. You can then load the xml file into an XML editor of your choice, and click yourself deeper into the search tree in places you want to understand better.

One good choice of an XML editor is [XML mind](https://www.xmlmind.com/xmleditor/).
