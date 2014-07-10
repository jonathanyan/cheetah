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

# resolve links - $0 may be a softlink

######################################################################
## Change by you BEGIN                                              *
######################################################################
#export CHEETAH_HOST=8000@127.0.0.1:8001@127.0.0.1
export CHEETAH_HOST=8000@127.0.0.1:8001@127.0.0.1:8002@127.0.0.1
export CHEETAH_THREAD_NUMBER=2
export CHEETAH_TREE_NUMBER=3
export CHEETAH_OPTS="-Xmx128m -Xms64m"
export LION_OPTS="-Xmx128m -Xms64m"
export SLEEP_MILLISECOND=2
export RETRY_MILLISECOND=3000
export N_REQUEST_MILLISECOND=20
export HTTP_SERVICE_PORT=8081

######################################################################
## Change by you END                                                *
######################################################################

# Non chagne below
this="${BASH_SOURCE-$0}"
common_bin=$(cd -P -- "$(dirname -- "$this")" && pwd -P)
script="$(basename -- "$this")"
this="$common_bin/$script"

# convert relative path to absolute path
config_bin=`dirname "$this"`
script=`basename "$this"`
cheetah_home=`cd "$config_bin/.."; pwd`
config_bin=`cd "$config_bin"; pwd`
this="$config_bin/$script"

# the root of the Cheetah installation

export CHEETAH_HOME=${cheetah_home}
#echo $CHEETAH_HOME

