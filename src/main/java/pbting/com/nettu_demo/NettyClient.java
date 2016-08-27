package pbting.com.nettu_demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

	public static void main(String[] args) {
		Bootstrap bootstrap = new Bootstrap();
		
		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		
		bootstrap.group(eventLoopGroup);
		
		bootstrap.channel(NioSocketChannel.class);
		
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new ChannelHandlerAdapter(){
					
					private int count ;
					
					private byte[] respon ;
					
					@Override
					public void channelActive(ChannelHandlerContext ctx)
							throws Exception {
						respon =
								("QUERY TIME ORDER"+System.getProperty("line.separator")).getBytes();
					
						ByteBuf byteBuf = null ;
						
						for(int i=0;i<10;i++){
							byteBuf = Unpooled.copiedBuffer(respon);
							ctx.writeAndFlush(byteBuf);
							Thread.sleep(500);
						}
					}
					
					@Override
					public void channelRead(ChannelHandlerContext ctx,
							Object msg) throws Exception {
						
						ByteBuf buffer = (ByteBuf)msg;
						
						byte[] res = new byte[buffer.readableBytes()];
						
						buffer.readBytes(res);
						
						String nowTime = new String(res,"UTF-8");
						
						System.out.println("[A]now time from server is :"+nowTime+"("+(++count)+")");
					}
					
					@Override
					public void channelReadComplete(ChannelHandlerContext ctx)
							throws Exception {
					
						ctx.close();
					}
				});
			}
		});
		
		try {
			bootstrap.connect("127.0.0.1", 4444).sync().channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
