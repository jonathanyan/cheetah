import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class CheetahServerHandler extends ChannelInboundHandlerAdapter {
    final private int GENERALMESSAGELENGTH=32;
    BTree bTreeRoot = null;
    

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object request) {
        ByteBuf in = (ByteBuf) request;
        ByteBuf response = ctx.alloc().buffer(GENERALMESSAGELENGTH);
        Fruit fruit;
        boolean close = false;
        int[] inArray= new int[GENERALMESSAGELENGTH/4];

        System.out.print("In: ");
        for ( int i=0; i<GENERALMESSAGELENGTH/4; i++ ) {
            inArray[i] = (int) in.readUnsignedInt();
            System.out.print(" - " + inArray[i]);
        }
        
        if ( inArray[3] == 6 ) close= true;
        System.out.println();
        System.out.println();
        System.out.flush();
  
        if ( bTreeRoot == null ) bTreeRoot = new BTree();
        fruit = new Fruit(inArray[1]);
        bTreeRoot = bTreeRoot.insertNewFruit(fruit, null, bTreeRoot, -1);
        int[] retInt = new int[8];
        retInt =bTreeRoot.first7Fruit(bTreeRoot, 0, 0, retInt); 
        bTreeRoot.traversalTree("MAIN", bTreeRoot, 0);

        response.writeInt((int) inArray[2]);
        for ( int i=1; i<GENERALMESSAGELENGTH/4; i++ ) {
            response.writeInt((int) (retInt[i-1]));
        }
        
        ChannelFuture future = ctx.writeAndFlush(response);
        
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

