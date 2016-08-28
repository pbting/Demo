package pbting.com.net.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

import pbting.com.net.nio.NioDeEnCoder;

public class MyNioDownloadServer {
	
	private final static HashMap<String, String> clientMap = new HashMap<String, String>();
	public static void main(String[] args) {
		try {
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			Selector selector = Selector.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			serverSocketChannel.bind(new InetSocketAddress(8009));
			while(true){
				int c = selector.select();
				if(c>0){
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
					while(keys.hasNext()){
						SelectionKey key = keys.next();
						keys.remove();
						if(key.isAcceptable()){
							try {
								ServerSocketChannel server = (ServerSocketChannel) key.channel();
								SocketChannel socketChannel = server.accept();
								socketChannel.configureBlocking(false);
								socketChannel.register(selector, SelectionKey.OP_READ);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}else if(key.isReadable()){
							SocketChannel socketChannel = (SocketChannel) key.channel();
							ByteBuffer fileNameBuffer = ByteBuffer.allocate(1024);
							socketChannel.read(fileNameBuffer);
							fileNameBuffer.flip();
							String fileName = NioDeEnCoder.getVarStrFromBuf(fileNameBuffer, 0, fileNameBuffer.limit());
							System.out.println("file name:"+fileName+";remote addrsess:"+socketChannel.getRemoteAddress().toString());
							socketChannel.register(selector, SelectionKey.OP_WRITE);
							clientMap.put(socketChannel.getRemoteAddress().toString(), fileName);
						}else if(key.isWritable()){
							SocketChannel socketChannel = (SocketChannel) key.channel();
							System.out.println("remote address"+socketChannel.getRemoteAddress().toString());
							String fileName = clientMap.get(socketChannel.getRemoteAddress().toString());
							System.out.println("file name[writer]"+fileName);
							File file = new File(fileName);
							FileChannel fileChannel = new FileInputStream(file).getChannel();
							fileChannel.transferTo(0, file.length(), socketChannel);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
