# Four In A Row

This is an implementation of a game engine in Java. The user interaction happens in the most simple way via system console.

## How to run it

You can simply load the project in your favourite Java IDE and hit the "Execute" button.

## Stand-alone Version

You can create a stand-alone version in the form of a jar file. I provided a script `build.sh` that will create a jar file, assuming the .class files have been created by the IntelliJ IDEA IDE. Your mileage might vary. A ready-made jar file is included in the git repository. You can execute it like this:

	java -jar four.jar

and play interactively in the console:

![Terminal](four_terminal.png)

## Debugging the Stand-alone Version

One purpose of this project is to gain a better understanding of the underlying minimax search. Even at low game settings the hierarchical search tree gets quite big in terns of number of positions. A linear trace log would be very long and impossible to read for a human reader. Therefore the trace log is saved as an XML structure which allows you to browse the search tree in a hierarchical way.

You create the XML trace log by starting the game with the command line option `debug`, for instance:

	java -jar four.jar debug

The game will create a log file named `debug001.xml` or similar, in the current working directory. You can then load the file into an XML editor of your choice, and click yourself deeper into the search tree in places you want to understand better.

One good choice of an XML editor is [XML mind](https://www.xmlmind.com/xmleditor/). A debug session might look like this:

![XML Debugging](xml_debug.png)

## Mobile App

Playing the console version is already fun and has a certain old-school flair. However, playing on a mobile device is even more fun, with a better user experience and the ability to play anywhere anytime.

We have added a simple Android app to this repository. You find the source code in the subdirectory `android`. You can add this directory as a new project in Android Studio, or whichever development environment you prefer. This way you can install it on your device or an emulator, modify the source code, debug it, etc.

For those just wanting to play, the app is available on [Google Play](https://play.google.com/store/apps/details?id=com.mr.four). Watch out for the app icon:

![App Icon](android/app/src/main/res/mipmap-hdpi/ic_launcher.png)

For those without access to Google Play we provide the [release build](android/app/release/app-release.apk) in this repository. Simply copy it to your device and "open" it to install it.

The mobile app has a [data privacy statement](android/DataProtection.md). A couple of screenshots in daylight and night mode:

![Light](four_light.png)
![Dark](four_dark.png)

### Localisation

We are open to including more languages into the app. Please send us a pull request, or a translated [strings.xml](android/app/src/main/res/values/strings.xml) file.

### Shareware Attributions

The app uses the following fonts and sound clips:

Resource | License Statement
------------ | -------------
[https://www.1001freefonts.com/gear-head.font](https://www.1001freefonts.com/gear-head.font) | "My fonts are free for noncommercial use and are donationware for commercial purposes."
[https://freesound.org/people/benjaminharveydesign/sounds/350428/](https://freesound.org/people/benjaminharveydesign/sounds/350428/) | [CC0 1.0 Universal (CC0 1.0)](https://creativecommons.org/publicdomain/zero/1.0/)
[https://freesound.org/people/primordiality/sounds/78824/](https://freesound.org/people/primordiality/sounds/78824/) | [Attribution 3.0 Unported (CC BY 3.0)](https://creativecommons.org/licenses/by/3.0/)
[https://freesound.org/people/rhodesmas/sounds/320657/](https://freesound.org/people/rhodesmas/sounds/320657/) | [Attribution 3.0 Unported (CC BY 3.0)](https://creativecommons.org/licenses/by/3.0/)
[https://freesound.org/people/Robinhood76/sounds/98874/](https://freesound.org/people/Robinhood76/sounds/98874/) | [Attribution-NonCommercial 3.0 Unported (CC BY-NC 3.0)](https://creativecommons.org/licenses/by-nc/3.0/)
