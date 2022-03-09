# 🎨 Macro-Processor
A short assembly macro-processor script to simulate the process and show the different stages

## 📝 Note 
1. This script does not simulate nested macros... Will be done in a future update
2. In case you don't want to specify output file a new one will be made on your desktop
3. In case your desktop is not found a new folder will be created in your user home directory and a output file will be made there

## Java Version < 11 peeps 🧧
Run this first in the same directory where the downloaded file is being kept

```terminal
javac MacroProcessor.java
```

**To run the code make of use these commands.**

_Note in case of missing parameters, default ones will be selected._

_Here ```"inputfile.txt"``` is the path and file name of file which contains the assembly code._

_Also ```"outputfile.txt"``` is the path and file name of your custom output file._

_If the output file specified by you, has not been created, the code will handle it._

```terminal
java MacroProcessor "inputFile.txt" "outputFile.txt"
```

_In case where no output parameter is specified, the code will create one on your desktop._

```terminal
java MacroProcessor "inputFile.txt"
```

_In case where no parameters are provided the code will scan for a file named ```"scan_for_macro.txt"``` on your desktop._

```terminal
java MacroProcessor
```

## Java Version > 11 peeps ✨
**Run the code directly using these commands**

_Note in case of missing parameters, default ones will be selected._

_Here ```"inputfile.txt"``` is the path and file name of file which contains the assembly code._

_Also ```"outputfile.txt"``` is the path and file name of your custom output file._

_If the output file specified by you, has not been created, the code will handle it._

```terminal
java MacroProcessor.java "inputFile.txt" "outputFile.txt"
```

_In case where no output parameter is specified, the code will create one named ```"macro"``` on your desktop._

```terminal
java MacroProcessor.java "inputFile.txt"
```

_In case where no parameters are provided, the code will scan for a file named ```"scan_for_macro.txt"``` on your desktop._

```terminal
java MacroProcessor.java
```


