package pbting.com.nettu_demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.sql.Date;

public class NettyServer {

	public static void main(String[] args) {
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		EventLoopGroup bossEvent = new NioEventLoopGroup();
		
		EventLoopGroup workerEvent = new NioEventLoopGroup();
		
		try {
			
			//1、
			serverBootstrap.group(bossEvent, workerEvent);
			
			//2、管道
			serverBootstrap.channel(NioServerSocketChannel.class);
			
			//3、在管道上添加各种事件处理器
			serverBootstrap.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ChannelInitializer<SocketChannel>(){

				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					sc.pipeline().addLast(new ChannelHandlerAdapter(){
						private int count = 0 ;
						@Override
						public void channelRead(ChannelHandlerContext ctx,
								Object msg) throws Exception {
							//在这里处理信息的传递
							ByteBuf buffer = (ByteBuf)msg;
							
							byte[] infors = new byte[buffer.readableBytes()];
							
							buffer.readBytes(infors);
							
							String body = new String(infors,"UTF-8").substring(0, infors.length-System.getProperty("line.separator").length());
							
							System.out.println("the time server reciver body is:["+body+"],and the count is :"+(++count));
							//发送
							
							//如果是查询时间命令，则返回正确的时间否则返回错误的编码
							String currentTime = 
									"QUERY TIME ORDER ".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD QUERY ORDER";
							
							//发送到客户端
							currentTime += System.getProperty("line.separator");
						
							ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
							
							ctx.writeAndFlush(resp);
						}
					});
				}
			});
			System.out.println("bind in 10004");
			//4、开始
			serverBootstrap.bind(4444).sync().channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			bossEvent.shutdownGracefully();
			workerEvent.shutdownGracefully();
		}
	}
}
