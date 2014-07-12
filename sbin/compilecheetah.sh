#!/usr/bin/env bash
sbin=`dirname "$0"`
sbin=`cd "$sbin"; pwd`
lib=`cd "$sbin"/../lib; ls`
echo "LIB=" $lib
libList=(${lib// / })

CHEETAHCLASSPATH=$sbin/../lib/${libList[0]}:$sbin/../lib/${libList[1]}:$sbin/../bin
echo "CLASSPATH=" $CHEETAHCLASSPATH

cd $sbin/../src; javac com/jontera/*.java -classpath $CHEETAHCLASSPATH -d $sbin/../bin; cd -

