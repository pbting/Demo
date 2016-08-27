package pbting.com.nettu_demo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import sun.misc.Unsafe;

/**
 * 使用semaphore实现电影晚上选座系统
 * @author pbting
 */
public class SemaphoreTick {
	static final int TICK_COUNT = 100;
	static final int TIMES = 10;
	volatile int[] seats = new int[TICK_COUNT];// 初始情况下每个座位的值为0，表示还未订
	private static sun.misc.Unsafe UNSAFE;
	private static long nextOffset;

	static {
		try {
			UNSAFE = getUnsafe();
			Class clazz = SemaphoreTick.class;
			nextOffset = UNSAFE.objectFieldOffset(clazz.getDeclaredField("seats"));
			System.out.println(nextOffset + "<-------");
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private static Unsafe getUnsafe() {
		try {
			Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
			singleoneInstanceField.setAccessible(true);
			return (Unsafe) singleoneInstanceField.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String[] args) {
		//同一时间只能允许有100个人同时选座
		Semaphore semaphore = new Semaphore(75);
		SemaphoreTick semaphoreTick = new SemaphoreTick();
		final Map<Integer, String> userTicks = Collections.synchronizedMap(new TreeMap<Integer,String>());
		//等所有的线程执行完，统计是否所有的票已卖完
		final long start = System.currentTimeMillis();
		CyclicBarrier cyclicBarrier = new CyclicBarrier(TICK_COUNT*TIMES,new Runnable() {
			@Override
			public void run() {
				int index = 0 ;
				for(Entry<Integer, String> entry: userTicks.entrySet()){
					if(index%5==0)
						System.out.println();
					System.out.print(entry.getKey()+"="+entry.getValue()+"\t");
					index++;
				}
				System.out.println("一共有："+userTicks.size()+"买票,处理共耗时："+(System.currentTimeMillis()-start));
			}
		});
		//如果有1000个人同时 订座，
		for(int i=0;i<TICK_COUNT*TIMES;i++){
			new Thread(new TakeTickThread(userTicks,cyclicBarrier,semaphore, semaphoreTick),"pbting_"+(i+1)).start();
		}
	}
	
	/**
	 * 订座位：如果定做成功，则返回该座位号。使用CAS 方式来实现多线程下的安全性
	 * @return
	 */
	public int takeTick(){
		//保留一份副本数据
		final int[] tmpSeats = Arrays.copyOf(seats, seats.length);
		//这个时候呢对tmpSeats 操作
		for(int i=0;i<tmpSeats.length;i++){
			if(tmpSeats[i]!=0)
				continue;
			//这个时候可能挥别切刀其他线程执行，在回到当前线程执行时，可能状态也发现改变，所以
			if(!(tmpSeats[i] ==seats[i]&&seats[i]==0)){
				continue;
			}
//			return (i+1);
			//这里更新需要CAS
			if(seats[i]==0&&casUpdateIndex(0, 1, i))
				return (i+1);
		}
		
		return -1;//票已售完
	}
	private static AtomicInteger atomicIneger = new AtomicInteger();
	public int takeTickAtomic(){
		int tickNum = 0 ;
		final int value = atomicIneger.getAndIncrement();
		//这个时候呢对tmpSeats 操作
		for(int i=0;i<seats.length;i++){
			if(seats[i]!=0)
				continue;
			tickNum = (i+1);
			if(value != atomicIneger.decrementAndGet()){
				continue;
			}else{
				return tickNum;
			}
		}
		
		return -1;//票已售完
	}
	
	public int takeTickOne(){
		//保留一份副本数据
		int index = 0 ;
		reStart:for(;;){
			final int[] repliSeats = this.seats;
			final int[] tmpSeats = Arrays.copyOf(seats, seats.length);
			// 这个时候呢对tmpSeats 操作
			for (int i = index; i < tmpSeats.length; i++) {
				if (tmpSeats[i] != 0)
					continue;
				// 这个时候可能挥别切刀其他线程执行，在回到当前线程执行时，可能状态也发现改变，所以
				if (!(tmpSeats[i] == seats[i] && seats[i] == 0)) {
					continue;
				}
				tmpSeats[i]=1;
				if(campareAndSweep(repliSeats, tmpSeats)){
					return (i+1);
				}else{
					index = (i-1)%2+1;
					continue reStart;
				}
			}
			break reStart;
		}
		return -1;
	}
	private boolean campareAndSweep(int[] cmp,int[] val){
	     return UNSAFE.compareAndSwapObject( this, nextOffset, cmp, val);
	}
//	private volatile boolean semaphore =false ;
	ReentrantLock lock = new ReentrantLock();
	private boolean casUpdateIndex(int expect,int val,int index){
//		for (; semaphore;);
		lock.lock();
		try{
		if (seats[index] == expect) {
			seats[index] = val;
			return true;
		}
		}finally{
			lock.unlock();
		}
		return false;
	}
}


class TakeTickThread implements Runnable{
	Semaphore semaphore ;
	SemaphoreTick semaphoreTick ;
	CyclicBarrier cyclicBarrier;
	Map<Integer, String> userTicks;
	public TakeTickThread(Map<Integer, String> userTicks,CyclicBarrier cyclicBarrier,Semaphore semaphore,SemaphoreTick semaphoreTick) {
		this.semaphore = semaphore;
		this.semaphoreTick = semaphoreTick;
		this.cyclicBarrier = cyclicBarrier;
		this.userTicks = userTicks;
	}
	
	@Override
	public void run() {
		try {
			this.semaphore.acquire();
//			int tickNum = this.semaphoreTick.takeTickAtomic();//非安全
//			int tickNum = this.semaphoreTick.takeTickOne();//非安全
//			int tickNum = this.semaphoreTick.takeTick();//无锁安全
			int tickNum = this.semaphoreTick.takeTickOne();//无锁安全
			if(tickNum>0){
				if(this.userTicks.containsKey(Integer.valueOf(tickNum))){
					System.out.println(this.userTicks.get(Integer.valueOf(tickNum))+"*****已经售出该票******"+tickNum);
				}
				this.userTicks.put(Integer.valueOf(tickNum),Thread.currentThread().getName());
			}
			this.semaphore.release();
			
			this.cyclicBarrier.await(20,TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}