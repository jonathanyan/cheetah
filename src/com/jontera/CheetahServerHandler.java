package com.jontera;

import io.netty.buffer.ByteBuf;

//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

public class CheetahServerHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = Logger.getLogger(CheetahServerHandler.class
            .getName());
    final private int hostId;
    final private int cheetahTreeNumber;
    private final NonBlockingHashMap<Integer, BTree> cheetahCache;
    private final NonBlockingHashMap<Integer, BTree> secondaryCheetahCache;
    private static boolean[] is2ndCacheActive;
    private String threadName;
    long threadId;

    // BTree bTreeRoot = null;

    CheetahServerHandler(int hostId, int cheetahTreeNumber,
            NonBlockingHashMap<Integer, BTree> cheetahCache,
            NonBlockingHashMap<Integer, BTree> secondaryCheetahCache,
            boolean[] is2ndCacheActive_s) {
        super();
        this.hostId = hostId;
        this.cheetahTreeNumber = cheetahTreeNumber;
        this.cheetahCache = cheetahCache;
        this.secondaryCheetahCache = secondaryCheetahCache;
        is2ndCacheActive = is2ndCacheActive_s;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {

        this.threadName = Thread.currentThread().getName();
        this.threadId = Thread.currentThread().getId();
        log.info("Start handleAdded at " + this.threadName + " id "
                + this.threadId);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        this.threadName = Thread.currentThread().getName();
        log.info("HandleRemoved at " + this.threadName);
    }

    public String treeProcess(BTree bTreeRoot, String line) {

        String[] lineArray = line.split("\\@", -1);
        int fruitId = Integer.parseInt(lineArray[1].trim());
        String[] commandArray = lineArray[0].trim().split("\\=", -1);
        String commandHead = commandArray[0];

        // no paramater
        Class noparams[] = {};

        // String parameter
        Class[] paramString = new Class[1];
        paramString[0] = String.class;

        String commandVar = null;
        if (commandArray.length > 1) {
            commandVar = commandArray[1].trim();
        }

        if (commandHead.startsWith("dumpTree")) {
            Apple apple = new Apple();
            String fileName = "../dat/DATADUMP_" + this.hostId;
            try {
                PrintWriter writer = new PrintWriter(fileName, "UTF-8");
                for (int i = 0; i < cheetahTreeNumber; i++) {
                    if (cheetahCache.containsKey(new Integer(i))) {
                        bTreeRoot = cheetahCache.get(new Integer(i));
                        apple.dumpAppleTree(bTreeRoot, 0, writer);
                    }
                }
                writer.close();
            } catch (Exception ex) {
                // report
            }
            return "Dump tree successful";
        }
        Apple apple = (Apple) bTreeRoot.findFruit(new Fruit(new FruitKey(
                fruitId)));
        if (apple == null) {
            System.out.println("Process NOT FOUND");
            return "NOT FOUND @key " + fruitId;
        } else {
            System.out.println("RECEIVE : " + commandHead + " -" + commandVar
                    +  "-  @ " + fruitId);
        }

        Method method = null;
        try {

            if (commandArray.length > 1) {
                try {
                    method = Apple.class.getDeclaredMethod(commandHead,
                            paramString);
                } catch (Exception e) {
                    System.out.println("Failed when 1 param invoke ");
                    e.printStackTrace();
                }

                if (null != method) {
                    method.invoke(apple, commandVar);

                    return (commandHead + " successful");
                }
            } else {
                method = Apple.class.getDeclaredMethod(commandHead, noparams);
                if (null != method) {
                    return (commandHead + " = " + method.invoke(apple,
                            new Object[0]));
                }

            }
        } catch (Exception e) {
            // doesn't matter
        }

        return "OK";
    }

    public ByteBuf channelProcess(ChannelHandlerContext ctx, Object request) {
        ByteBuf response = ctx.alloc().buffer(Packet.GENERALMESSAGELENGTH);
        ByteBuf in = (ByteBuf) request;

        Integer mapKey;
        HashBucket hashVal = new HashBucket();
        BTree bTreeRoot = null;

        int sequenceNumber = in.readInt();
        int hostId = (int) in.readShort();
        byte commandType = in.readByte();
        byte commandLength = in.readByte();
        byte[] buf = new byte[commandLength];

        for (int i = 0; i < commandLength; i++)
            buf[i] = in.readByte();

        System.out.println("CheetahSeverHandler commandType=" + commandType
                + " hostID=" + this.hostId + " received hostId=" + hostId);

        if (commandType == 100) {
            is2ndCacheActive[0] = true;
            return null;
        }
        String retLine;
        try {
            String line = new String(buf, "US-ASCII");
            String[] lineArray = line.split("\\@", -1);

            int fruitId = Integer.parseInt(lineArray[1].trim());

            mapKey = new Integer(hashVal.hashBucket(fruitId, cheetahTreeNumber));

            if (hostId == this.hostId) {
                if (!cheetahCache.containsKey(mapKey)) {
                    retLine = new String("Not found " + fruitId);
                } else {
                    bTreeRoot = cheetahCache.get(mapKey);
                    retLine = treeProcess(bTreeRoot, line);
                }
            } else {
                if (!secondaryCheetahCache.containsKey(mapKey)) {
                    retLine = new String("Not found " + fruitId);
                } else {
                    bTreeRoot = secondaryCheetahCache.get(mapKey);
                    retLine = treeProcess(bTreeRoot, line);
                }
            }

            if (commandType < 100
                    && (hostId == this.hostId || is2ndCacheActive[0])) {
                Packet packet = new Packet(sequenceNumber, (short) hostId,
                        retLine);
                packet.messageToByte(response);
                return response;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object request) {
        // ByteBuf response = ctx.alloc().buffer(Packet.GENERALMESSAGELENGTH);

        ByteBuf response = channelProcess(ctx, request);
        if (response != null) {
            ctx.writeAndFlush(response);
        }
        // ChannelFuture future = ctx.writeAndFlush(response);

        // if (close) {
        // future.addListener(ChannelFutureListener.CLOSE);
        // }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // this.threadName = Thread.currentThread().getName();
        log.info("Channel broken at handler");
        cause.printStackTrace();
        ctx.close();
    }
}
