#!/bin/bash

if [ -r out/production/four/com/mr/four/Main.class ] ; then
	cd out/production/four
	jar -cvfm ../../../four.jar ../../../Manifest.txt com/mr/four/*.class
	cd ../../..
else
	echo "IDEA-generated class files not found."
fi
