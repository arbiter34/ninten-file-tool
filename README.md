## BYML Editor

If you're on Windows use the batch scripts to allow for drag and drop of 1 or more items.
They'll Decode/Encode in the directory you drag n' dropped from.

#### BYML to JSON
```
java -jar ninten-file-tool.jar --decode --byml --input ActorInfo.product.sbyml --output ActorInfo.product.json
```

#### JSON to BYML
```
java -jar ninten-file-tool.jar --encode --byml --input ActorInfo.product.json --output ActorInfo.product.sbyml
```

#### PrOD to JSON
```
java -jar ninten-file-tool.jar --decode --prod --input infile.sblwp --output infile.json
```

#### JSON to PrOD
```
java -jar ninten-file-tool.jar --encode --prod --input infile.json --output infile.sblwp
```

#### Resources:

https://github.com/HandsomeMatt/botw-modding/blob/master/docs/file_formats/byml.md

http://mk8.tockdom.com/wiki/BYAML_(File_Format)