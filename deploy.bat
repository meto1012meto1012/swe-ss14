@ECHO OFF

rem T = Threads
rem C = Core-multiplied, d.h. bei 8 Kernen gilt:
rem     1C = 1 * 8 = 8 Threads
rem     1.5C = 1.5 * 8 = 12 Threads
rem o = offline

mvn -DskipTests -T 1C -o %* wildfly:deploy-only
pause
