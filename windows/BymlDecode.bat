@ECHO OFF
SET "dir=%~dp0"
:Loop
IF "%1" == "" GOTO Done
java -jar "%dir%/ninten-file-tool.jar" --decode --byml --input "%1" --output "%1.json"
SHIFT
GOTO Loop
:Done