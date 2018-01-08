@ECHO OFF
SET "dir=%~dp0"
:Loop
IF "%1" == "" GOTO Done
SET "filepath=%1" 
SET "searchstring=.json"
SET "replacestring="
CALL SET "modifiedpath=%%filepath:%searchstring%=%replacestring%%%"
:: Belt-and-braces
SET "modifiedpath=%modifiedpath:\=/%"
java -jar "%dir%/ninten-file-tool.jar" c p "%1" "%modifiedpath%"
SHIFT
GOTO Loop
:Done