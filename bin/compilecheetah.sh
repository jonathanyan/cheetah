#!/usr/bin/env bash
bin=`dirname "$0"`
bin=`cd "$bin"; pwd`
lib=`cd "$bin"/../lib; ls`
echo "LIB=" $lib
libList=(${lib// / })

CHEETAHCLASSPATH=$bin/../lib/${libList[0]}:$bin/../lib/${libList[1]}:$bin
echo "CLASSPATH=" $CHEETAHCLASSPATH

cd $bin/../src; javac com/jontera/*.java -classpath $CHEETAHCLASSPATH -d $bin; cd -

