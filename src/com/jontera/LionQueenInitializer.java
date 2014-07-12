package com.jontera;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class LionQueenInitializer extends ChannelInitializer<SocketChannel> {
    private static int sequenceNumber;
    private static int cheetahThreadNumber;
    private static int cheetahHostNumber;
    private static NonBlockingHashMap<Integer, Packet> lionCache;
    private static NonBlockingHashMap<Integer, Packet> lionHandleCache;
    private static int[] threadHostIndex;
    private static boolean[] isActiveByCheetah;
    private static boolean[] isActiveLion;
    private static int[][] requestTimeStamp;
    private static int[] requestHostIndex;
    private static int requestTimeLowerLimit;
    private int sleepMilliSecond;
    private static int retryMilliSecond;

    public LionQueenInitializer() {

    }

    public LionQueenInitializer(int sequenceNumber_s,
            int cheetahThreadNumber_s, int cheetahHostNumber_s,
            NonBlockingHashMap<Integer, Packet> lionCache_s,
            NonBlockingHashMap<Integer, Packet> lionHandleCache_s,
            int[] threadHostIndex_s, boolean[] isActiveByCheetah_s,
            boolean[] isActiveLion_s, int[][] requestTimeStamp_s,
            int[] requestHostIndex_s, int retryMilliSecond_s, int sleepMilliSecond, int requestTimeLowerLimit_s) {
        sequenceNumber = sequenceNumber_s;
        cheetahHostNumber = cheetahHostNumber_s;
        cheetahThreadNumber = cheetahThreadNumber_s;
        lionCache = lionCache_s;
        lionHandleCache = lionHandleCache_s;
        threadHostIndex = threadHostIndex_s;
        isActiveByCheetah = isActiveByCheetah_s;
        isActiveLion = isActiveLion_s;
        requestTimeStamp = requestTimeStamp_s;
        requestHostIndex = requestHostIndex_s;
        retryMilliSecond = retryMilliSecond_s;
        this.sleepMilliSecond = sleepMilliSecond;
        requestTimeLowerLimit = requestTimeLowerLimit_s;
    }

    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new HttpServerCodec());
        p.addLast(new LionQueenHandler(sequenceNumber, cheetahThreadNumber,
                cheetahHostNumber, lionCache, lionHandleCache, threadHostIndex,
                isActiveByCheetah, isActiveLion, requestTimeStamp,
                requestHostIndex, retryMilliSecond, sleepMilliSecond, requestTimeLowerLimit));
    }
}
