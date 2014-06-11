import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class CheetahDecoder extends ByteToMessageDecoder {
    final private int GENERALMESSAGELENGTH=32; 
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < GENERALMESSAGELENGTH) {
            return;
        }

        out.add(in.readBytes(GENERALMESSAGELENGTH));
    }
}

