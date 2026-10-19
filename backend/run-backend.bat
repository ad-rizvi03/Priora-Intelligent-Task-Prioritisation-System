@echo off
set "JAVA_HOME=C:\Users\S M ADNAN RIZVI\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT"
call "%~dp0mvnw.cmd" -s settings.xml spring-boot:run
