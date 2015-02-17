**Before you start, be aware that the current FairplayMP requires multiple physical/virtual machines to run it.**

Reference: [[Readme](http://www.cs.huji.ac.il/project/Fairplay/FairplayMP/Readme.txt)]

Prerequisite
===

``ant 1.8``

FairplayMP version 1 instruction file.
===

Stage 1 - SFDL program:
---

1. Write your program in SFDL2.0 language, you can use the SFLD2.0 specification and examples in the project web site. (You can see the SecondPriceAuction.sfdl example in the package).
2. From inside the compiler directory, compile your program using the command:

```
cp sfdl/SecondPriceAuction.sfdl compiler_v1_built/SecondPriceAuction-tocompile.sfdl
cd compiler_v1_built/compiler
java lab.Runner -f ../SecondPriceAuction-tocompile.sfdl
cd ..
ruby Convertor.rb SecondPriceAuction-tocompile.sfdl
# generating files with .cnv, .Opt.circuit , .Opt.fmt
mv *.cnv ..
mv *.Opt.circuit ..
mv *.Opt.fmt ..
```

OR

```
java  -cp compiler_v2_built/ sfdl.Compiler sfdl/SecondPriceAuction.sfdl 
```

(Compiler source code is not provided)

Stage 2 - Running the secure multiparty computation:
---

1. Create the ``config.xml`` file fit to the SFDL program. (You can use the ``config.xml`` example in the package - just replace the ip addresses).

2. Deploy the package to each participating computer and run:

```
java -cp runtime/build/classes FairplayMP <randomSeed>
```
