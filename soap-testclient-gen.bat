@ECHO OFF

REM Voraussetzung: Deployment von shop.war im laufenden WildFly Appserver

%JBOSS_HOME%\bin\wsconsume -v -k ^
                           -p de.shop.kundenverwaltung.soap.gen ^
                           -s src/test/java ^
                           -o target/test-classes ^
                           -t 2.2 ^
                           %JBOSS_HOME%\standalone\data\wsdl\shop.war\KundeSOAPService.wsdl
