The current framework requires multiple physical/virtual machines to run it.

Reference: [[Readme](http://www.cs.huji.ac.il/project/Fairplay/FairplayMP/Readme.txt)]

FairplayMP version 1 instruction file.
===

Stage 1 - SFDL program:
---

1. Write your program in SFDL2.0 language, you can use the SFLD2.0 specification and examples in the project web site. (You can see the SecondPriceAuction.sfdl example in the package).
2. From inside the compiler directory, compile your program using the command:
```
java  -cp compiler_built/ sfdl.Compiler sfdl/SecondPriceAuction.sfdl 
```
3. Convert the file using the command:

```
ruby Convertor.rb <filename>
Example: ruby Convertor.rb SecondPriceAuction.sfdl
```

Stage 2 - Running the secure multiparty computation:
---

1. Create the config.xml file fit to the SFDL program. (You can use the config.xml example in the package - just replace the ip addresses).
2. Deploy the package to each participating computer and run:

```
java FairplayMP <randomSeed>
```
