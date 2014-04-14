#!/bin/sh

JACC_VERSION=1.0.0.Final
EJB_VERSION=1.0.0.Final
GUAVA_VERSION=16.0.1

JBOSS_MODULES=$JBOSS_HOME/modules/system/layers/base

CLASSPATH=target/classes;
CLASSPATH=$CLASSPATH;$JBOSS_MODULES/javax/security/jacc/api/main/jboss-jacc-api_1.5_spec-$JACC_VERSION.jar
CLASSPATH=$CLASSPATH;$JBOSS_MODULES/javax/ejb/api/main/jboss-ejb-api_3.2_spec-$EJB_VERSION.jar
CLASSPATH=$CLASSPATH;$JBOSS_MODULES/com/google/guava/main/guava-$GUAVA_VERSION.jar

java -cp $CLASSPATH de.shop.auth.business.AuthService $*
