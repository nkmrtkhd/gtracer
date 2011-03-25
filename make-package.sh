#!/bin/bash

echo "#################"
echo "making GTracer.zip"
echo "#################"
echo ""

#emacs viewer/UpdateNotifier.java
emacs UpdateManager.java

ver=`grep -e "thisVersion=" UpdateManager.java |grep -o "[0-9].*" |sed s/\;//`

ant clean
ant jar
mkdir GTracer
cp GTracer.jar GTracer/
cp ReadMe.txt GTracer/
zip -r GTracer-$ver.zip GTracer/
rm -rfv GTracer/


# find . -name '*.java' -exec sed -i -e 's/jogamp/sun/' {} \;
# find . -name "*.java-e" -exec rm -v {} \;
