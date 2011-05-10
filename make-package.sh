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
mkdir GTracer-$ver
cp GTracer.jar GTracer-$ver/
cp ReadMe.txt GTracer-$ver/
cp sample_Rutile_O_K.png GTracer-$ver/
zip -r GTracer-$ver.zip GTracer-$ver/
rm -rfv GTracer-$ver/


# find . -name '*.java' -exec sed -i -e 's/jogamp/sun/' {} \;
# find . -name "*.java-e" -exec rm -v {} \;
