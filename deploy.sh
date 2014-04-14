#!/bin/sh

# T = Threads
# C = Core-multiplied, d.h. bei 8 Kernen gilt:
#     1C = 1 * 8 = 8 Threads
#     1.5C = 1.5 * 8 = 12 Threads
# o = offline
mvn -DskipTests -T 1C -o $* wildfly:deploy-only
