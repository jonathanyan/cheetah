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


# Start all lion daemons.  Run this on mast node.

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`
LOGDIR=`cd "$bin/../log"; pwd`
rm $LOGDIR/*.log
cat /dev/null >$LOGDIR/lion.log

if [ -e "$bin/cheetah-config.sh" ]; then
  . "$bin"/cheetah-config.sh
fi


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
done

nohup $bin/cheetah.sh -p 8000 -h 0 >$LOGDIR/cheetah_0.log 2>&1 &
nohup $bin/cheetah.sh -p 8001 -h 1 >$LOGDIR/cheetah_1.log 2>&1 &
nohup $bin/cheetah.sh -p 8002 -h 2 >$LOGDIR/cheetah_2.log 2>&1 &
sleep 3
nohup $bin/lion.sh >$LOGDIR/lion.log 2>&1 &
tail -f $LOGDIR/lion.log


