package com.jontera;

import java.util.logging.Logger;
import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Process any incoming data.
 */
public class CheetahServer {
    private static Logger log = Logger.getLogger(CheetahServerHandler.class
            .getName());
    final private int hostId;
    final private int port;
    final private int cheetahTreeNumber;
    final private int cheetahHostNumber;
    final private String cheetahHomeDir;
    private static final NonBlockingHashMap<Integer, BTree> cheetahCache = new NonBlockingHashMap<Integer, BTree>();
    private static final NonBlockingHashMap<Integer, BTree> secondaryCheetahCache = new NonBlockingHashMap<Integer, BTree>();
    private static boolean[] is2ndCacheActive = { false };

    public CheetahServer(int hostId, int port, int cheetahTreeNumber,
            int cheetahHostNumber, String cheetahHomeDir) {
        this.hostId = hostId;
        this.port = port;
        this.cheetahTreeNumber = cheetahTreeNumber;
        this.cheetahHostNumber = cheetahHostNumber;
        this.cheetahHomeDir = cheetahHomeDir;
        (new Forest()).reloadTree(cheetahHomeDir + "/dat/cheetah_" + hostId
                + ".dat", cheetahTreeNumber, cheetahCache);
        (new Forest()).reloadTree(cheetahHomeDir + "/dat/cheetah_"
                + ((hostId + 1) % cheetahHostNumber) + ".dat",
                cheetahTreeNumber, secondaryCheetahCache);
        // is2ndCacheActive = false;
    }

    public void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(
                                    new CheetahDecoder(),
                                    new CheetahServerHandler(hostId,
                                            cheetahTreeNumber, cheetahCache,
                                            secondaryCheetahCache,
                                            is2ndCacheActive));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to
            // gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("Cheetah Server interrupred !!!!!!!!!!!!!!!!!!!!!!");
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {

        Map<String, String> env = System.getenv();
        int port = Integer.parseInt(env.get("PORT_NUMBER"));
        int cheetahTreeNumber = Integer
                .parseInt(env.get("CHEETAH_TREE_NUMBER"));
        int cheetahHostId = Integer.parseInt(env.get("CHEETAH_HOST_ID"));
        int cheetahHostNumber = Integer
                .parseInt(env.get("CHEETAH_HOST_NUMBER"));
        String cheetahHomeDir = env.get("CHEETAH_HOME");

        if (args.length == 2) {
            port = Integer.parseInt(args[0]);
            cheetahHostId = Integer.parseInt(args[1]);
        }
        log.info("Cheetah Server (Host:" + cheetahHostId + " Port:" + port
                + ") load data ... ");
        CheetahServer cheetahServer = new CheetahServer(cheetahHostId, port,
                cheetahTreeNumber, cheetahHostNumber, cheetahHomeDir);
        // new CheetahServer(port, cheetahTreeNumber).run();

        log.info("Cheetah Server started - listen at " + port);
        cheetahServer.run();

    }
}
