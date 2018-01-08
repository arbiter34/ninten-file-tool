@ECHO OFF
SET "dir=%~dp0"
:Loop
IF "%1" == "" GOTO Done
java -jar "%dir%/ninten-file-tool.jar" d b "%1" "%1.json"
SHIFT
GOTO Loop
:Done