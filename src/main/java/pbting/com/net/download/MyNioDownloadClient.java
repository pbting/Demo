package pbting.com.net.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import pbting.com.net.nio.NioDeEnCoder;

public class MyNioDownloadClient {

	public static void main(String[] args) {
		try {
			SocketChannel socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			Selector selector = Selector.open();
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
			socketChannel.connect(new InetSocketAddress(8009));
			while(true){
				int count = selector.select();
				if(count>0){
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
					while(keys.hasNext()){
						SelectionKey key = keys.next();
						keys.remove();
						if(key.isConnectable()){
							try {
								SocketChannel client = (SocketChannel) key.channel();
								if(client.isConnectionPending()){
									client.finishConnect();
								}
								//发送需要下载的文件
								ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
								byteBuffer.put("D:/demo.txt".getBytes());
								byteBuffer.flip();
								client.write(byteBuffer);
								client.register(selector, SelectionKey.OP_READ);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}else if(key.isReadable()){
							SocketChannel client = (SocketChannel) key.channel();
							ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
							client.read(byteBuffer);
							System.out.println(NioDeEnCoder.getVarStrFromBuf(byteBuffer, 0, byteBuffer.limit()));
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
