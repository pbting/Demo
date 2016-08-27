package pbting.com.nettu_demo;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SemaphoreTest {

	public static void main(String[] args) {
		Semaphore semaphore = new Semaphore(1);
		ReentrantLock reentrantLock = new ReentrantLock();
		ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
		//synchorized
		SemaphoreTest st = new SemaphoreTest();
		final long start = System.currentTimeMillis();
		int N = 5000;
		CyclicBarrier cyclicBarrier = new CyclicBarrier(N*2,new Runnable() {
			@Override
			public void run() {
				System.out.println("--->耗时"+(System.currentTimeMillis()-start));
			}
		});
		//5000个线程写
		for(int i=0;i<N*2;i++){
			if(i%2==0)
				new Thread(new UpdateVal(readWriteLock,reentrantLock, st,semaphore,cyclicBarrier)).start();
			else
				new Thread(new ReadVal(readWriteLock,reentrantLock, st,semaphore,cyclicBarrier)).start();
		}
	}
	
	private int sequence ;
	public void increVal(Semaphore semaphore){
		try {
			semaphore.acquire();
			++sequence;
			semaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void getSequence(Semaphore semaphore){
		try {
			semaphore.acquire();
			System.out.println("getSequence:-->"+(sequence));
			semaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void increVal(){
		++sequence;
	}
	public void getSequence(){
		System.out.println("getSequence:-->"+(sequence));
	}
	
	public synchronized void increSyn(){
		++sequence;
	}
	public synchronized void getSyn(){
		System.out.println("-->"+(++sequence));
	}
	
	public synchronized void increLovk(ReentrantLock reentrantLock){
		reentrantLock.lock();
		++sequence;
		reentrantLock.unlock();
	}
	public synchronized void getSequence(ReentrantLock reentrantLock){
		reentrantLock.lock();
		System.out.println("-->"+(sequence));
		reentrantLock.unlock();
	}
	
	public synchronized void increRWLock(ReadWriteLock readWriteLock){
		readWriteLock.writeLock().lock();
		++sequence;
		readWriteLock.writeLock().unlock();
	}
	public synchronized void getSequence(ReadWriteLock readWriteLock){
		readWriteLock.readLock().lock();
		System.out.println("-->"+(sequence));
		readWriteLock.readLock().unlock();
	}
}


class UpdateVal implements Runnable{
	private SemaphoreTest st ;
	private Semaphore semaphore ;
	CyclicBarrier cyclicBarrier ;
	ReentrantLock reentrantLock ;
	ReadWriteLock readWriteLock ;
	public UpdateVal(ReadWriteLock readWriteLock,ReentrantLock reentrantLock,SemaphoreTest st,Semaphore semaphore,CyclicBarrier cyclicBarrier) {
		this.st = st ;
		this.semaphore = semaphore;
		this.cyclicBarrier = cyclicBarrier;
		this.reentrantLock = reentrantLock;
		this.readWriteLock = readWriteLock;
	}
	public void run() {
		for(int i=0;i<10;i++){
//			st.increVal();//非线程安全
//			st.increVal(semaphore);//带信号量
//			st.increSyn();//synchorized
//			st.increLovk(reentrantLock);
			st.increRWLock(this.readWriteLock);
		}
		try {
			cyclicBarrier.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
	};
}

class ReadVal implements Runnable{
	private SemaphoreTest st ;
	private Semaphore semaphore ;
	CyclicBarrier cyclicBarrier ;
	ReentrantLock reentrantLock ;
	ReadWriteLock readWriteLock ;
	public ReadVal(ReadWriteLock readWriteLock,ReentrantLock reentrantLock,SemaphoreTest st,Semaphore semaphore,CyclicBarrier cyclicBarrier) {
		this.st = st ;
		this.semaphore = semaphore;
		this.cyclicBarrier = cyclicBarrier;
		this.reentrantLock = reentrantLock;
		this.readWriteLock = readWriteLock;
	}
	public void run() {
		for(int i=0;i<10;i++){
//			st.getSequence();//线程非安全下获取
//			st.getSequence(semaphore);//带信号量 获取
//			st.getSyn();//synchorized
//			st.getSequence(reentrantLock);//reentrantLock 读
			st.getSequence(this.readWriteLock);//读写分离锁
		}
		try {
			cyclicBarrier.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
	};
}
