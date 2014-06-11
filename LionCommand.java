import io.netty.buffer.ByteBuf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class LionCommand {
	final private static int GENERALMESSAGELENGTH = 32;
	static final int SIZE = 15;

	public static void main(String[] args) throws Exception {
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		int randStart = Integer.parseInt(args[2]);
		RandomSeed rs = new RandomSeed(100);
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new LionCommandHandler());
				}
			});

			// Start the client.
			Channel ch = b.connect(host, port).sync().channel();
			ChannelFuture lastWriteFuture = null;

			try {
				for (int i = 0; i < 7; i++) {
					ByteBuf out = ch.alloc().buffer(GENERALMESSAGELENGTH);
					out.writeInt((int) 4);
					out.writeInt((int) (rs.randomNumber[randStart + i]));
					out.writeInt((int) port);
					out.writeInt((int) i);

					for (int j = 4; j < GENERALMESSAGELENGTH / 4; j++) {
						out.writeInt((int) 0);
					}
					
					lastWriteFuture = ch.writeAndFlush(out);

				}
				
				ch.closeFuture().sync();

				if (lastWriteFuture != null) {
					lastWriteFuture.sync();
				}

			} catch (Exception io) {
				io.printStackTrace();
			}

			// Wait until the connection is closed. 
			// ch.closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}
}
