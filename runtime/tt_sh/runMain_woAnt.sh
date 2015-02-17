#!/usr/bin/env bash
main_executable=main.MainExecutable
lib_dir=./lib

for f in $lib_dir/*.jar ; do
  classpath=$(pwd)/$f:$classpath
done
build_jar=$(ls build/jar/*.jar)
classpath=$(pwd)/${build_jar}:$classpath
#here, dont use -jar option which is mutually exclusive of -classpath
java -classpath $classpath $main_executable
