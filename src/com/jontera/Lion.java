package com.jontera;

//import java.util.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Map;
import java.util.logging.Logger;

//import java.util.concurrent.*;
import org.cliffc.high_scale_lib.*;

public class Lion implements Runnable {
    private static Logger log = Logger.getLogger(CheetahServerHandler.class
            .getName());

    private static int sequenceNumber = 0;
    private static int ctrlSequenceNumber = 0;
    private static int cheetahThreadNumber;
    private static int cheetahHostNumber;
    private static int lionThreadNumber;
    private static int httpPort;
    private static NonBlockingHashMap<Integer, Packet> lionCache;
    private static NonBlockingHashMap<Integer, Packet> lionHandleCache;
    private static int[] threadHostIndex;
    private static byte[] isActiveByCheetah;
    private static boolean[] isActiveLion = { true };
    private static int[][] requestTimeStamp;
    private static int[] requestHostIndex;
    private static int requestTimeLowerLimit;
    private static int[] lionHandleCacheSnapShot;
    private int hostId;
    private int port;
    private int threadId;
    private String hostName;
    private static int sleepMilliSecond;
    private static int retryMilliSecond;
    final private static int MAX_USED_INTEGER = 1000000000;

    public Lion(int hostId, int port, int threadId, String hostName) {
        this.hostId = hostId;
        this.port = port;
        this.threadId = threadId;
        this.hostName = hostName;
    }

    @Override
    public void run() {

        if (this.hostId == -1) { // Lion thread - HTTP service
            try {
                lionQueen();
            } catch (Exception e) {

            }
        } else if (this.hostId == -2) { // Lion thread - house keeping
            try {
                lionKing();
            } catch (Exception e) {

            }
        } else { // lion thread - Communicate with CHEETAH server
            try {
                lionBaby();
            } catch (Exception e) {

            }
        }

    }

    public void lionQueen() throws Exception {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(
                            new LionQueenInitializer(sequenceNumber,
                                    cheetahThreadNumber, cheetahHostNumber,
                                    lionCache, lionHandleCache,
                                    threadHostIndex, isActiveByCheetah,
                                    isActiveLion, requestTimeStamp,
                                    requestHostIndex, retryMilliSecond,
                                    sleepMilliSecond, requestTimeLowerLimit));

            Channel ch = b.bind(httpPort).sync().channel();

            System.err
                    .println("Open your web browser and navigate to http://127.0.0.1:8081 ");

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void sendLionKingCtrlCommand(int hostSeq, byte commandType) {
        int k, index = 0;
        try {
            Packet packet = new Packet(ctrlSequenceNumber, (short) hostSeq,
                    (byte) 100, (byte) 0);

            for (k = 0; k < cheetahThreadNumber; k++) {
                index = (threadHostIndex[hostSeq] + k + 1)
                        % cheetahThreadNumber;
                if (!lionCache.containsKey(new Integer(hostSeq
                        * cheetahThreadNumber + index))) {
                    threadHostIndex[hostSeq] = index;
                    break;
                }
            }

            if (k < cheetahThreadNumber) {
                lionCache.put(
                        new Integer(hostSeq * cheetahThreadNumber + index),
                        packet);
            } else {
                System.out.println("Lion King Ctrl Command " + commandType
                        + " host " + hostSeq + " seq " + ctrlSequenceNumber);
            }
        } catch (Exception e) {

        }
    }

    public void lionKing() throws Exception {
        boolean[] isHouseKeeping = new boolean[cheetahHostNumber];
        int timeOutHostId = -1;

        for (int i = 0; i < cheetahHostNumber; i++) {
            isHouseKeeping[i] = false;
        }

        for (;;) {

            /* System is down */
            if (!isActiveLion[0]) {
                System.out.println("House Keeping -- System not in service");
                // send shutdown command
                break;
            }

            timeOutHostId = -1;
            for (int i = 0; i < cheetahHostNumber; i++) {
                /* Beep time out server with command type 99 */
                if (isActiveByCheetah[i] == 3) {
                    timeOutHostId = i;
                    System.out.println("Lion King beep time out Cheetah " + i);
                    ctrlSequenceNumber--;
                    sendLionKingCtrlCommand(i, (byte) 99);
                    break;
                }
            }

            for (int i = 0; i < cheetahHostNumber; i++) {
                /* Active secondary node processing if primary node is down */
                if ((isActiveByCheetah[i] == 0) && !isHouseKeeping[i]) {
                    isHouseKeeping[i] = true;
                    System.out.println("House Keeping -- Cheetah " + i
                            + " down");
                    int hostSeq = (i + cheetahHostNumber - 1)
                            % cheetahHostNumber;
                    if (isActiveByCheetah[hostSeq] == 5 ) {
                        // Active secondary CHEETAH
                        ctrlSequenceNumber--;
                        sendLionKingCtrlCommand(hostSeq, (byte) 100);
                    } else {
                        System.out
                                .println("House Keeping -- 2 contagious Cheetah "
                                        + hostSeq
                                        + " down, System not in service");
                    }
                }

            }

            /* Remove timeout response packet from return buffer */
            for (int i = 0; i < lionThreadNumber; i++) {
                Packet packet;

                if ((packet = lionHandleCache.get(new Integer(i))) != null) {
                    if (packet.sequenceNumber == lionHandleCacheSnapShot[i]) {
                        lionHandleCache.remove(new Integer(i));
                        System.out
                                .println("LionKing - remove time out return - seq "
                                        + packet.sequenceNumber);
                    } else {
                        lionHandleCacheSnapShot[i] = packet.sequenceNumber;
                    }
                }

            }
            Thread.sleep(retryMilliSecond);
            if (timeOutHostId >= 0) {
                if (isActiveByCheetah[timeOutHostId] == 3) {
                    isActiveByCheetah[timeOutHostId] = 0;
                    System.out.println("Lion King beep no return from "
                            + timeOutHostId);
                }
            }
        }
    }

    public void lionBaby() throws Exception {
        RetryLoop: for (;;) {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            Channel ch = null;

            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new LionBabyHandler(lionHandleCache, hostId,
                                        threadId, cheetahThreadNumber,
                                        sleepMilliSecond, retryMilliSecond,
                                        isActiveByCheetah));
                    }
                });

                // Start the client.
                for (;;) {
                    try {
                        ch = b.connect(hostName, this.port).sync().channel();
                        if (ch != null) {
                            isActiveByCheetah[this.hostId] = 5;
                            break;
                        }
                    } catch (Exception e) {
                        ch = null;
                        log.info("** commander lion connection failed. Retry ...");
                        Thread.sleep(retryMilliSecond);
                    }
                }

                ChannelFuture lastWriteFuture = null;
                log.info("Commander Lion host " + this.hostId + " thread "
                        + Thread.currentThread().getName());

                Packet packet;
                int emptyLoop = 0;
                for (;;) {
                    if (!isActiveLion[0]) {
                        workerGroup.shutdownGracefully();
                        System.out.println("System shutdown received at "
                                + this.hostId);
                        break RetryLoop;
                    }
                    if (isActiveByCheetah[this.hostId] == 0) {
                        workerGroup.shutdownGracefully();
                        System.out.println("Host " + this.hostId
                                + " not working");
                        break RetryLoop;
                    }
                    packet = null;

                    if ((packet = lionCache.get(new Integer(this.hostId
                            * cheetahThreadNumber + this.threadId))) != null) {
                        emptyLoop = 0;
                        ByteBuf out = ch.alloc().buffer(
                                Packet.GENERALMESSAGELENGTH);
                        packet.messageToByte(out);
                        try {
                            lastWriteFuture = ch.writeAndFlush(out);
                        } catch (Exception e) {
                            workerGroup.shutdownGracefully();
                            break RetryLoop;
                        }
                        lionCache.remove(new Integer(this.hostId
                                * cheetahThreadNumber + this.threadId));

                    } else {
                        emptyLoop++;
                        Thread.sleep(sleepMilliSecond);

                    }

                    if (isActiveByCheetah[this.hostId] == 0) {
                        workerGroup.shutdownGracefully();

                        System.out.println("Host " + this.hostId
                                + " is DOWN ???!!!");
                        break RetryLoop;
                    }

                    if (emptyLoop > 100) {
                        emptyLoop = 0;
                        if (this.threadId == cheetahThreadNumber - 1) {
                            // send beep command
                        }
                    }
                } // end of for loop

                // ch.closeFuture().sync();

                // if (lastWriteFuture != null) {
                // lastWriteFuture.sync();
                // }

                // Wait until the connection is closed.
                // ch.closeFuture().sync();

            } catch (Exception e) {
                log.info("** commander lion connection broken - Tried again");
            } finally {
                workerGroup.shutdownGracefully();
            }
        } // end RetryLoop
    }

    public static void main(String[] args) throws Exception {

        Map<String, String> env = System.getenv();

        cheetahThreadNumber = Integer
                .parseInt(env.get("CHEETAH_THREAD_NUMBER"));

        final String[] hostArray = env.get("CHEETAH_HOST").split("\\:", -1);

        cheetahHostNumber = hostArray.length;

        lionThreadNumber = cheetahThreadNumber * cheetahHostNumber;

        httpPort = Integer.parseInt(env.get("HTTP_SERVICE_PORT"));

        lionCache = new NonBlockingHashMap<Integer, Packet>(lionThreadNumber);

        lionHandleCache = new NonBlockingHashMap<Integer, Packet>(
                lionThreadNumber);
        lionHandleCacheSnapShot = new int[lionThreadNumber];

        threadHostIndex = new int[cheetahHostNumber];

        isActiveByCheetah = new byte[cheetahHostNumber];

        retryMilliSecond = Integer.parseInt(env.get("RETRY_MILLISECOND"));
        sleepMilliSecond = Integer.parseInt(env.get("SLEEP_MILLISECOND"));
        requestTimeLowerLimit = Integer.parseInt(env
                .get("N_REQUEST_MILLISECOND"));

        requestTimeStamp = new int[cheetahHostNumber][cheetahThreadNumber];
        requestHostIndex = new int[cheetahHostNumber];

        Lion[] lions = new Lion[cheetahThreadNumber];

        for (int i = 0; i < cheetahHostNumber; i++) {
            threadHostIndex[i] = -1;
            isActiveByCheetah[i] = 5;
            requestHostIndex[i] = 0;

            String[] portHost = hostArray[i].split("\\@", -1);
            log.info("port " + portHost[0] + " host " + portHost[1]);

            for (int k = 0; k < cheetahThreadNumber; k++) {
                requestTimeStamp[i][k] = 0;
                lionHandleCacheSnapShot[i * cheetahThreadNumber + k] = 1 - MAX_USED_INTEGER;
                lions[k] = new Lion(i, Integer.parseInt(portHost[0]), k,
                        portHost[1]);
            }

            for (int k = 0; k < cheetahThreadNumber; k++) {
                try {
                    new Thread(lions[k]).start();
                } catch (RuntimeException e) {
                    log.info("** RuntimeException from main");
                }
            }
        }

        try {
            new Thread(new Lion(-1, -1, -1, "localhost")).start(); // Lion
            new Thread(new Lion(-2, -2, -2, "localhost")).start(); // Lion
            // handle
        } catch (RuntimeException e) {
            log.info("** RuntimeException from main");
        }

    }
}
