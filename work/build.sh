#!/bin/bash
javac -classpath "../lib/weka.jar" -d . ../src/moxworx/*.java
jar cvfm ../bin/moxworx.jar moxworx.mf moxworx
