@ECHO OFF
SETLOCAL

SET JACC_VERSION=1.0.0.Final
SET EJB_VERSION=1.0.0.Final
SET GUAVA_VERSION=16.0.1

SET JBOSS_MODULES=%JBOSS_HOME%\modules\system\layers\base

SET CLASSPATH=target/classes;
SET CLASSPATH=%CLASSPATH%;%JBOSS_MODULES%\javax\security\jacc\api\main\jboss-jacc-api_1.5_spec-%JACC_VERSION%.jar
SET CLASSPATH=%CLASSPATH%;%JBOSS_MODULES%\javax\ejb\api\main\jboss-ejb-api_3.2_spec-%EJB_VERSION%.jar
SET CLASSPATH=%CLASSPATH%;%JBOSS_MODULES%\com\google\guava\main\guava-%GUAVA_VERSION%.jar

java -cp %CLASSPATH% de.shop.auth.business.AuthService %*

ENDLOCAL
