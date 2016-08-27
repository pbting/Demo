package pbting.com.nettu_demo;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CyclicBarrierTest {

	public static void main(String[] args) {
		ExecutorService service = Executors.newCachedThreadPool();

		final CyclicBarrier cb = new CyclicBarrier(100,new Runnable() {
			int count = 0 ;
			@Override
			public void run() {
				System.out.println("----->任务处理一次"+(++count));
			}
		});//开指定线程size 去处理并发时的任务
		for (int i = 0; i < 499; i++) {
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						Thread.sleep((long) (Math.random() * 10000));
//						System.out.println("线程"+ Thread.currentThread().getName()
//								+ "即将到达集合地点 1，当前已有"+ (cb.getNumberWaiting() + 1)+ "个已到达"
//								+ (cb.getNumberWaiting() == 4 ? "都到齐了，继续走啊": "正在等候"));
						try {
							cb.await(10,TimeUnit.SECONDS);
							//主任务 和子任务的连接点 就是在这里实现。主任务只管当前有多少个任务执行完了。每个子任务完成后向我汇报一下即可
						} catch (BrokenBarrierException e) {
							e.printStackTrace();
						}catch(TimeoutException e){
							e.printStackTrace();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			service.execute(runnable);
		}
		service.shutdown();
	}
}
