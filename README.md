## ninten-file-tools

<b>sbyml/sbwlp/smubin/baniminfo encode/decode with Yaz0 (byml/prod w/ yaz0)</b>

If you're on Windows use the batch scripts to allow for drag and drop of 1 or more items.
They'll Decode/Encode in the directory you drag n' dropped from.

###Note
While you can use the command line, it's suggested to use the batch file if you are on Windows.  The no-compress
batch file is meant for things like baniminfo which aren't compressed with yaz0.
#### BYML to JSON
```
java -jar ninten-file-tool.jar --input ActorInfo.baniminfo
```

#### JSON to BYML
```
java -jar ninten-file-tool.jar --input ActorInfo.baniminfo.json
```

#### PrOD to JSON
```
java -jar ninten-file-tool.jar --input infile.sblwp
```

#### JSON to PrOD
```
java -jar ninten-file-tool.jar --input infile.sblwp.json
```

#### Resources:

https://github.com/HandsomeMatt/botw-modding/blob/master/docs/file_formats/byml.md

http://mk8.tockdom.com/wiki/BYAML_(File_Format)