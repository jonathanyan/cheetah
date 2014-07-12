#!/usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# Start all cheetah daemons.  Run this on slave node.
echo $PASSARG
while [[ $# > 1 ]]
do
key="$1"
shift

case $key in
    -p|--port)
    CHEETAHPORT="$1"
    shift
    ;;
    -h|--host)
    CHEETAHHOST="$1"
    shift
    ;;
    --default)
    DEFAULT=YES
    shift
    ;;
    *)
            # unknown option
    ;;
esac
done

sbin=`dirname "$0"`
sbin=`cd "$sbin"; pwd`

if [ -e "$sbin/cheetah-config.sh" ]; then
  . "$sbin"/cheetah-config.sh
  . "$sbin"/../run/cheetah-port.sh
fi

lib=`cd "$sbin"/../lib; ls`
echo "LIB=" $lib
libList=(${lib// / })
CHEETAHCLASSPATH=$sbin/../lib/${libList[0]}:$sbin/../lib/${libList[1]}:$sbin/../bin

if [ "$PASSARG" = "0" ]; then
cd "$sbin";java -classpath $CHEETAHCLASSPATH $CHEETAH_OPTS com.jontera.CheetahServer
else
cd "$sbin";java -classpath $CHEETAHCLASSPATH $CHEETAH_OPTS com.jontera.CheetahServer $CHEETAHPORT $CHEETAHHOST
fi

