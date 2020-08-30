@ECHO OFF
set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;../lib/jclust.jar
set CLASSPATH=%CLASSPATH%;../lib/commons-cli-1.4.jar
set CLASSPATH=%CLASSPATH%;../lib/commons-math3-3.6.1.jar
set CLASSPATH=%CLASSPATH%;../lib/log4j-api-2.3.jar
set CLASSPATH=%CLASSPATH%;../lib/log4j-core-2.3.jar

java clustering.main.AlgorithmRunnerMain -paramfile kmtdparam.csv -numthread 2 -prefix syn -vindex RunTime:WithinClusterSS:CorrectedRandIndex

pause