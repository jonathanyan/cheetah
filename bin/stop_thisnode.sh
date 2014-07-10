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

TMPDIR=${bin}/../run
ps -ef | grep ${USER} | grep 'java\|tail' | grep -v vi | grep -v http| grep -v grep | awk '{print "kill -9 " $2 }' > $TMPDIR/kill_all.ksh
sh $TMPDIR/kill_all.ksh
ps -ef | grep ${USER} | grep java


