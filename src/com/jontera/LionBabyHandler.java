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
    private static int[] noReturnCommandCountByCheetah;
    private int sleepMilliSecond;
    private static int retryMilliSecond;

    LionBabyHandler(NonBlockingHashMap<Integer, Packet> lionHandleCache,
            int hostId, int threadId, int cheetahThreadNumber,
            int[] noReturnCommandCountByCheetah_s, int sleepMilliSecond,
            int retryMilliSecond_s) {
        super();
        this.lionHandleCache = lionHandleCache;
        this.hostId = hostId;
        this.threadId = threadId;
        this.cheetahThreadNumber = cheetahThreadNumber;
        noReturnCommandCountByCheetah = noReturnCommandCountByCheetah_s;
        this.sleepMilliSecond = sleepMilliSecond;
        retryMilliSecond = retryMilliSecond_s;
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
        noReturnCommandCountByCheetah[this.hostId]--;
        // packet.print();
        in.release();

        final int index = this.hostId * this.cheetahThreadNumber
                + this.threadId;
        for (int i = 0; i < retryMilliSecond / sleepMilliSecond / 2; i++) {
            if (!lionHandleCache.containsKey(new Integer(index))) {

                lionHandleCache.put(new Integer(index), packet);
                break;
            } else {
                if (i == retryMilliSecond / sleepMilliSecond / 2 - 1) {
                    System.out.println("RESPONSE_BUFFER_JAM at seq "
                            + packet.sequenceNumber);
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
