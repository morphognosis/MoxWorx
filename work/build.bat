javac -classpath "../lib/morphognosis.jar;../lib/weka.jar" -d . ../src/morphognosis/moxworx/*.java
jar cvfm ../bin/moxworx.jar moxworx.mf morphognosis
