@echo off
set "JAVA_HOME=C:\Users\akshat.chandawar\.vscode\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64"
set "MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT"
"C:\Users\akshat.chandawar\.vscode\extensions\oracle.oracle-java-26.0.1\nbcode\java\maven\bin\mvn.cmd" -s settings.xml spring-boot:run
