package pbting.com.nettu_demo;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;


public class StringDemo {

	
	public static final int val = 64 ;
	public static int value = 32;
	
	static{
		System.out.println("val:"+val);
		System.out.println("value:"+value);
	}
	
	public static void main(String[] args) {
//		ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
//		
//		queue.add("PBTING");
//		
//		System.out.println(queue.size());
//		
//		String peekOb = queue.remove();
//		
//		System.out.print(peekOb+"-->"+queue.size());
//		
//		ConcurrentLinkedDeque<String> deque = new ConcurrentLinkedDeque<String>();
//		
//		deque.add("sdgf");
		
		arrayTest();
	}

	private static void taillTest() {
		String[] array = new String[10];
		array[0] = "7road_1";
		array[1] = "7road_2";
		array[2] = "7road_3";

		String tail = array[2];//指向当前数组中最后一个节点 
		
		String p = tail;//记录当前节点的最后一个节点
		
		//接下来有可能有其他线程网数组里面添加元素，是的最后结点发现变化
		array[3] = "7road_4";
		array[4] = "7road_5";
		
		tail = array[4];
		if((tail == p)){
			System.out.println("--->没有其他线程修改");
		}else{
			System.out.println("--->有其他线程修改");
		}
		System.out.println(p+"--->"+tail+"--->"+(tail == p));
	}
	private static void arrayTest() {
		String[] array = new String[10];
		array[0] = "7road_1";
		array[1] = "7road_2";
		array[2] = "7road_3";

		String[] tmpArray = array;
		
		//接下来有可能有其他线程网数组里面添加元素，是的最后结点发现变化
		tmpArray[3] = "7road_4";
		tmpArray[4] = "7road_5";

		System.out.println("--->"+tmpArray[3]+"--->"+array[3]);
	}
}
