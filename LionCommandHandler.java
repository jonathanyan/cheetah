import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class LionCommandHandler extends ChannelInboundHandlerAdapter {
    final private int GENERALMESSAGELENGTH=32;
    private ByteBuf buf;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buf = ctx.alloc().buffer(32); // (1)
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buf.release(); // (1)
        buf = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
/*
        UnixLion m = (UnixLion) msg;
        System.out.println(m);
        ctx.close();
*/
        ByteBuf m = (ByteBuf) msg;
        buf.writeBytes(m); 
        m.release();

        if (buf.readableBytes() >= GENERALMESSAGELENGTH) { 
            System.out.println("answer: " );
            for ( int i=0; i<GENERALMESSAGELENGTH/4; i++ ) {
                System.out.print(" - " + buf.readUnsignedInt());
                System.out.flush();
            }
            System.out.println();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
