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


# Prepare script.  Run this on mast node.

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

if [ -e "$bin/cheetah-config.sh" ]; then
  . "$bin"/cheetah-config.sh
fi

cat /dev/null > $bin/../run/shiptocheetah.sh

cheetahHost=(${CHEETAH_HOST//:/ })
hostSequence=-1
hostNumber="${#cheetahHost[@]}"
for x in "${cheetahHost[@]}";do
    portHost=(${x//@/ })
    portNumber="${portHost[0]}"
    hostAddr="${portHost[1]}"
    #echo $hostSequence
    (( hostSequence += 1 ))
    echo "export PORT_NUMBER=${portNumber}" > $bin/../run/cheetah-port.sh
    echo "export CHEETAH_HOST_ID=${hostSequence}" >> $bin/../run/cheetah-port.sh
    echo "export CHEETAH_HOST_NUMBER=${hostNumber}" >> $bin/../run/cheetah-port.sh
    remoteCheetahDir="cheetah_"${hostSequence}
    rm -rf  /tmp/${remoteCheetahDir}
    cp -r $bin/../ /tmp/${remoteCheetahDir}
    echo "scp -r /tmp/${remoteCheetahDir} $hostAddr:/tmp/" >>$bin/../run/shiptocheetah.sh
    #ssh $hostAddr "/tmp/${remoteCheetahDir}/cheetah.sh >/tmp/${remoteCheetahDir}/cheetah.log 2>&1 &"
done
