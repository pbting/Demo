package pbting.com.net.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


public class NetNioClient {

	public static void main(String[] args) {
		try {
			SocketChannel socketChannel = SocketChannel.open();
			Selector selector = Selector.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress("127.0.0.1", 8809));
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
			boolean isOver = false ;
			while(!isOver){
				selector.select();
				Iterator<SelectionKey> selIterator = selector.selectedKeys().iterator();
				while(selIterator.hasNext()){
					SelectionKey selectionKey = selIterator.next();
					selIterator.remove();
					if(selectionKey.isConnectable()){
						if(socketChannel.isConnectionPending()){
							if(socketChannel.finishConnect()){
								//连接成功后，服务端会发送key 和 版本信息。
								ByteBuffer readByteBuffer = ByteBuffer.allocate(1024);
								socketChannel.read(readByteBuffer);
								readByteBuffer.flip();
								int keyLenght = readByteBuffer.getInt();
								int position = readByteBuffer.position();
								String key = NioDeEnCoder.getVarStrFromBuf(readByteBuffer, position, position+keyLenght);
								int versonLength = readByteBuffer.getInt();
								position = readByteBuffer.position();
								String version = NioDeEnCoder.getVarStrFromBuf(readByteBuffer, position, position+versonLength);
								System.out.println("key:"+key+";version:"+version);
								socketChannel.close();
								if("prism_wyl_pbt".equals(key)&&"1.0.0".equals(version)){
									isOver = true ;
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
