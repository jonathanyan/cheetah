Cheetah
=======


Cheetah is a distributed in-memory dual-active application engine. It attempts to replace backend database for certain application and provides fast responses for high-concurrency requirements.

Cheetah loads all data in memory with duplication mode, and is able to work well even a couple of non-contagious nodes are down.

Here is Cheetah design chart link

https://github.com/jonathanyan/cheetah/blob/master/html/cheetah_dualactive2.JPG

Basically, there are 2 kinds of nodes

Lion - Master node, process http requirement and monitor cheetah healthy status

Cheetah - Slave node, store data in duplicate mode and handle Lion command 

For Lion node, there are 3 roles - thread groups

1. Lion Queen - Prcessing http requirement

2. Lion Baby - Communcate between Lion node and Cheetah nodes

3. Lion King - Monitor cheetah healthy status


Start Single Node
=======

Prerequisite

1. Linux node one or more

2. Install jdk 1.6 or above

3. Netty 4.0.19 or above from http://netty.io/

4. Highly Scallable Lib from http://sourceforge.net/projects/high-scale-lib/files/high-scale-lib/ 

5. (Optional for test) Install wget at Linux node

Start single node

1. Download cheetah package by clicking "Download Zip" from https://github.com/jonathanyan/cheetah

2. Unzip cheetah-master.zip, and it will create directory "cheetah-master". Then change direcotory mode

    ~$ unzip cheetah-master.zip

    ~$ chmod -R 755 cheetah-master

3. Copy "Netty" and "Highly Scallable Lib" jar files into cheetah-master/lib, 

    ~$ ls cheetah-master/lib

   You will see 2 jar files:  high-scale-lib.jar and netty-all-4.0.19.Final.jar

4. Compile java files

    ~$ cd  ~/cheetah-master/sbin; ./compilecheetah.sh

    Check if there is class files in "cheetah-master/bin" direcotory 

   ~$ ls ~/cheetah-master/bin/com/jontera

5. Run single node cheetah 

   ~$ cd  ~/cheetah-master/sbin; ./start_singlenode.sh   

6. Test cheetah

   ~$ cd ~/cheetah-master/test; ./foxget 1

   You will see response,

   RESPONSE: getDemand = 5723

7 Stop cheetah

   ~$ cd ~/cheetah-master/sbin; ./stop_thisnode.sh


Start Multiple Node
=======

Prerequisite

1. Single node can be run successfully 

Start multiple node

1. Edit cheetah-master/sbin/cheetah-config.sh to change cheetah host list 

   ~$ vi ~/cheetah-master/sbin/cheetah-config.sh

   export CHEETAH_HOST=8000@153.65.191.151:8000@153.65.191.152:8000@153.65.191.153

   The format is node list with (port_number@host_address) seperated by ":"

   This "CHEETAH_HOST" configuration is for cheetah host only. Lion host is current machine, not need to be configured.
 
2. Prepare cheetah host package

    ~$ cd ~/cheetah-master/sbin; ./prep_multinode.sh

    it will prepare package for cheetah node in /tmp

    ~$ ls /tmp/cheetah*

3. Ship package from Lion host to Cheetah hosts

    ~$ cd ~/cheetah-master/run; sh shiptocheetah.sh

    It will use "scp" command to copy package to cheetah nodes one by one

4. Login in Cheetah hosts one by one, start Cheetah Daemon, 

    ~$ /tmp/cheetah_X/sbin/cheetah.sh

    where _X is cheetah host number, X maybe 0, 1, 2, etc.

5. Back to Lion host, start Lion Daemon at Lion host,    

   cd ~/cheetah-master/sbin; ./lion.sh

6. Test cheetah

   ~$ cd cheetah-master/test; ./foxget 1

   You will see response,

   RESPONSE: getDemand = 5723

7. Stop cheetah and lion one by one 

   For Cheetah,

   ~$ cd /tmp/cheetah_0/sbin; ./stop_thisnode.sh

   For Lion,

   ~$ cd cheetah-master/sbin; ./stop_thisnode.sh


