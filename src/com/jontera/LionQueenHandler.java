package com.jontera;

import java.util.Set;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class LionQueenHandler extends SimpleChannelInboundHandler<Object> {
    private HttpRequest request;
    /** Buffer that stores the response content */
    private final StringBuilder buf = new StringBuilder();
    private static int sequenceNumber;
    private static int cheetahThreadNumber;
    private static int cheetahHostNumber;
    private static NonBlockingHashMap<Integer, Packet> lionCache;
    private static NonBlockingHashMap<Integer, Packet> lionHandleCache;
    private static int[] threadHostIndex;
    private static byte[] isActiveByCheetah;
    private static boolean[] isActiveLion;
    private static int[][] requestTimeStamp;
    private static int[] requestHostIndex;
    private static int retryMilliSecond;
    private int httpSequenceNumber = 0;
    private int sleepMilliSecond;
    private static int requestTimeLowerLimit;
    private long beginTime = 0, endTime = 0;
    final private static int MAX_USED_INTEGER = 1000000000;

    public LionQueenHandler(int sequenceNumber_s, int cheetahThreadNumber_s,
            int cheetahHostNumber_s,
            NonBlockingHashMap<Integer, Packet> lionCache_s,
            NonBlockingHashMap<Integer, Packet> lionHandleCache_s,
            int[] threadHostIndex_s, byte[] isActiveByCheetah_s,
            boolean[] isActiveLion_s, int[][] requestTimeStamp_s,
            int[] requestHostIndex_s, int retryMilliSecond_s,
            int sleepMilliSecond, int requestTimeLowerLimit_s) {
        // sequenceNumber = sequenceNumber_s;
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

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    protected int putRequestBuffer(HttpRequest request) {
        int i;
        int index = 0;
        int hostSeq = -1;
        String line = request.getUri().substring(1);
        if (line.startsWith("shutdownCheetah")) {
            System.out.println("COMMAND ==== SHUTDOWN =====");
            isActiveLion[0] = false;
            return -5;
        }
        System.out.println("LionQueenHandler - Request : " + line);

        try {
            byte[] b = line.getBytes("US-ASCII");

            if (b.length > Packet.BUFFERLENGTH)
                return hostSeq;

            String[] lineArray = line.split("\\@", -1);
            if (lineArray.length < 2) {
                System.out.println("Command syntax error. Retry...");
                return hostSeq;
            }

            int randStart = Integer.parseInt(lineArray[1].trim());
            hostSeq = (new HashBucket()).hashBucket(randStart,
                    cheetahHostNumber);
            int originalHostSeq = hostSeq;

            if (0 == isActiveByCheetah[hostSeq]) {
                hostSeq = (hostSeq + cheetahHostNumber - 1) % cheetahHostNumber;
                System.out.println("HOST status first (" + originalHostSeq
                        + " - " + isActiveByCheetah[originalHostSeq]
                        + ") second (" + hostSeq + " - "
                        + isActiveByCheetah[hostSeq] + ")");
                if (0 == isActiveByCheetah[hostSeq]) {
                    System.out.println("Server first and second both down!!!");
                    return -1;
                }
            } else if (3 == isActiveByCheetah[hostSeq]) {
                System.out.println("Cheetah host " + hostSeq
                        + " busy. Retry in 20 seconds...");
                return -2;

            }

            int nRequestTime = (int) ((this.beginTime - requestTimeStamp[hostSeq][requestHostIndex[hostSeq]]) % MAX_USED_INTEGER);
            requestTimeStamp[hostSeq][requestHostIndex[hostSeq]] = (int) (this.beginTime % MAX_USED_INTEGER);
            requestHostIndex[hostSeq] = (requestHostIndex[hostSeq] + 1)
                    % cheetahThreadNumber;

            if (nRequestTime < requestTimeLowerLimit) {
                return -3;
            }
            httpSequenceNumber = sequenceNumber++;

            Packet packet = new Packet(httpSequenceNumber,
                    (short) originalHostSeq, (byte) 0, (byte) b.length, b);

            for (i = 0; i < cheetahThreadNumber; i++) {
                index = (threadHostIndex[hostSeq] + i + 1)
                        % cheetahThreadNumber;
                if (!lionCache.containsKey(new Integer(hostSeq
                        * cheetahThreadNumber + index))) {
                    threadHostIndex[hostSeq] = index;
                    break;
                }
            }

            if (i < cheetahThreadNumber) {
                lionCache.put(
                        new Integer(hostSeq * cheetahThreadNumber + index),
                        packet);
                return hostSeq;
            } else {
                System.out.println("BUSYGOSSIP host " + hostSeq + " seq "
                        + packet.sequenceNumber + " in thread "
                        + Thread.currentThread().getName());
                return -1;
            }
        } catch (Exception e) {

        }

        return hostSeq;
    }

    public void getResponseBuffer(StringBuilder buf, int hostSeq)
            throws Exception {
        Packet packet;
        int iloop = 0, retry = 0;

        if (hostSeq < 0) {
            return;
        }
        int index = hostSeq * cheetahThreadNumber;
        for (;;) {

            if ((packet = lionHandleCache.get(new Integer(index))) != null) {
                if (httpSequenceNumber != packet.sequenceNumber)
                    System.out.println("httpSequenceNumber "
                            + httpSequenceNumber + " seq  "
                            + packet.sequenceNumber);
                if (httpSequenceNumber == packet.sequenceNumber) {
                    System.out.print("LionQueenHandler - Response : buf_index "
                            + index + " seq  " + packet.sequenceNumber);
                    buf.append("RESPONSE: " + packet.byteToString());
                    buf.append("\r\n");

                    lionHandleCache.remove(new Integer(index));
                    break;
                }
            }

            iloop++;
            index = hostSeq * cheetahThreadNumber + (index + 1)
                    % cheetahThreadNumber;

            if (iloop > cheetahThreadNumber) {
                Thread.sleep(sleepMilliSecond);
                iloop = 0;
                index = hostSeq * cheetahThreadNumber;
                retry++;
                if (retry < retryMilliSecond / sleepMilliSecond) {
                    continue;
                } else {
                    isActiveByCheetah[hostSeq] = 3;
                    System.out.print("Response Time out at host " + hostSeq
                            + " seq  " + httpSequenceNumber + " retry number "
                            + retry + " " + retryMilliSecond + " "
                            + sleepMilliSecond);
                    buf.append("99888 - Response Time out at seq  "
                            + httpSequenceNumber + (sequenceNumber - 1));
                    buf.append("\r\n");
                    break;
                }
            }
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            buf.setLength(0);

            this.beginTime = System.currentTimeMillis();
            int hostSeq = putRequestBuffer(request);
            if (isActiveLion[0] && hostSeq >= 0) {
                try {
                    Thread.sleep(sleepMilliSecond);

                    getResponseBuffer(buf, hostSeq);
                } catch (Exception e) {

                }
            } else if (hostSeq < 0) {
                if (hostSeq == -5) {
                    buf.append("99999 - System down ");
                } else if (hostSeq == -2) {
                    buf.append("99998 - Cheetah host busy, retry in 20 seconds ... ");
                } else if (hostSeq == -3) {
                    buf.append("99997 - Cheetah request jam, retry in a few seconds ... ");
                } else {
                    buf.append("99996 - Lion server busy, retry after a few seconds ... ");
                }
                buf.append("\r\n");

            } else {
                buf.append("SHUTDOWN received").append("\r\n\r\n");
            }
            this.endTime = System.currentTimeMillis();
            String stringTime = "" + (int) (this.endTime - this.beginTime);
            System.out.println(" WALLTIME "
                    + ("00000000" + stringTime).substring(stringTime.length()));

            appendDecoderResult(buf, request);
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();
            if (content.isReadable()) {
                buf.append("CONTENT: ");
                buf.append(content.toString(CharsetUtil.UTF_8));
                buf.append("\r\n");
                appendDecoderResult(buf, request);
            }

            if (msg instanceof LastHttpContent) {
                // buf.append("END OF CONTENT\r\n");

                LastHttpContent trailer = (LastHttpContent) msg;
                if (!trailer.trailingHeaders().isEmpty()) {
                    buf.append("\r\n");
                    for (String name : trailer.trailingHeaders().names()) {
                        for (String value : trailer.trailingHeaders().getAll(
                                name)) {
                            buf.append("TRAILING HEADER: ");
                            buf.append(name).append(" = ").append(value)
                                    .append("\r\n");
                        }
                    }
                    buf.append("\r\n");
                }

                if (!writeResponse(trailer, ctx)) {
                    // If keep-alive is off, close the connection once the
                    // content is fully written.
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
                            ChannelFutureListener.CLOSE);
                }
            }
        }
    }

    private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
        DecoderResult result = o.getDecoderResult();
        if (result.isSuccess()) {
            return;
        }

        buf.append(".. WITH DECODER FAILURE: ");
        buf.append(result.cause());
        buf.append("\r\n");
    }

    private boolean writeResponse(HttpObject currentObj,
            ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                currentObj.getDecoderResult().isSuccess() ? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH,
                    response.content().readableBytes());
            // Add keep alive header as per:
            // -
            // http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Encode the cookie.
        String cookieString = request.headers().get(COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = CookieDecoder.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (Cookie cookie : cookies) {
                    response.headers().add(SET_COOKIE,
                            ServerCookieEncoder.encode(cookie));
                }
            }
        } else {
            // Browser sent no cookie. Add some.
            response.headers().add(SET_COOKIE,
                    ServerCookieEncoder.encode("key1", "value1"));
            response.headers().add(SET_COOKIE,
                    ServerCookieEncoder.encode("key2", "value2"));
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}