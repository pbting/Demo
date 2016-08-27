package pbting.com.net.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NetNioServer {

	private static Selector selector;

	public static void main(String[] args) {
		try {
			ServerSocketChannel ssChannel = ServerSocketChannel.open();
			ssChannel.configureBlocking(false);// 异步
			ssChannel.bind(new InetSocketAddress(8809));
			selector = Selector.open();
			ssChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("start linstener....");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 开始监听客户端的连接
		while (true) {
			try {
				int count = selector.select();
				if (count > 0) {
					Iterator<SelectionKey> keys = selector.selectedKeys()
							.iterator();
					while (keys.hasNext()) {
						SelectionKey targetKey = keys.next();
						keys.remove();
						handler(targetKey);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// 准备好读取客户端的数据
	private static ByteBuffer readbuffer = ByteBuffer.allocate(1024);
	private static ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
	private static String randomKey = "prism_wyl_pbt";
	private static String VERSION = "1.0.0";

	private static void handler(SelectionKey targetKey) {
		// 拿到与客户端通信的管道
		SocketChannel clientChannel = null;
		try {
			if (targetKey.isAcceptable()) {
				ServerSocketChannel serverSocketChannel = (ServerSocketChannel) targetKey
						.channel();
				clientChannel = serverSocketChannel.accept();
				clientChannel.configureBlocking(false);
				initWriterClinetInfo();
				//这个时候就应该将数据发送给客户端
				clientChannel.write(writeBuffer);
			} else if (targetKey.isReadable()) {
				clientChannel = (SocketChannel) targetKey.channel();
				readbuffer.clear();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void initWriterClinetInfo() {
		writeBuffer.clear();
		byte[] randomKeyBytes = randomKey.getBytes(Charset.forName("utf-8"));
		//前四个字节记录版本信息的长度
		writeBuffer.putInt(randomKeyBytes.length);
		writeBuffer.put(randomKeyBytes);
		//当前的版本信息
		byte[] versionBytes = VERSION.getBytes(Charset.forName("utf-8"));
		writeBuffer.putInt(versionBytes.length);
		writeBuffer.put(versionBytes);
		writeBuffer.flip();//会将position 和limit 设置成数组里面的有效长度。capacity 仍然是1024
	}
}
