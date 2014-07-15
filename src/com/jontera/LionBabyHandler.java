package com.jontera;

//import java.util.logging.Logger;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LionBabyHandler extends ChannelInboundHandlerAdapter {

    private int hostId;
    private int threadId;
    final private int cheetahThreadNumber;
    private ByteBuf buf;
    private final NonBlockingHashMap<Integer, Packet> lionHandleCache;
    private int sleepMilliSecond;
    private static int retryMilliSecond;
    private static byte[] isActiveByCheetah;

    LionBabyHandler(NonBlockingHashMap<Integer, Packet> lionHandleCache,
            int hostId, int threadId, int cheetahThreadNumber,
            int sleepMilliSecond, int retryMilliSecond_s,
            byte[] isActiveByCheetah_s) {
        super();
        this.lionHandleCache = lionHandleCache;
        this.hostId = hostId;
        this.threadId = threadId;
        this.cheetahThreadNumber = cheetahThreadNumber;
        this.sleepMilliSecond = sleepMilliSecond;
        retryMilliSecond = retryMilliSecond_s;
        isActiveByCheetah = isActiveByCheetah_s;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buf = ctx.alloc().buffer(Packet.GENERALMESSAGELENGTH);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buf.release();
        buf = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ByteBuf in = (ByteBuf) msg;
        Packet packet = new Packet(in);
        System.out.println("LionBabyHandler - Response: "
                + packet.byteToString() + " seq " + packet.sequenceNumber);
        // packet.print();
        in.release();

        if (packet.commandType == 99) {
            isActiveByCheetah[packet.hostId] = 5;
            return;
        }
        final int index = this.hostId * this.cheetahThreadNumber
                + this.threadId;
        for (int i = 0; i < retryMilliSecond / sleepMilliSecond * 2 / 3; i++) {
            if (!lionHandleCache.containsKey(new Integer(index))) {

                lionHandleCache.put(new Integer(index), packet);
                break;
            } else {
                if (i == retryMilliSecond / sleepMilliSecond * 2 / 3 - 1) {
                    System.out.println("RESPONSE_BUFFER_JAM replace with seq "
                            + packet.sequenceNumber);
                    lionHandleCache.remove(new Integer(index));
                    lionHandleCache.put(new Integer(index), packet);
                }
                try {
                    Thread.sleep(sleepMilliSecond);
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
