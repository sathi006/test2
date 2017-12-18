/**
 * 
 */
package com.mcg.batch.core.support.threading;

import static com.mcg.batch.core.BatchConfiguration.THREAD_POOL_BACKLOG;
import static com.mcg.batch.core.BatchConfiguration.THREAD_POOL_KEEP_ALIVE_MS;
import static com.mcg.batch.core.BatchConfiguration.THREAD_POOL_MAX;
import static com.mcg.batch.core.BatchConfiguration.THREAD_POOL_MIN;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class SmartBatchExecutor extends ThreadPoolExecutor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchExecutor.class);

	/**
	 * prevent external instantiation
	 */
	private SmartBatchExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit timeUnit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit,
				workQueue, new SmartBatchThreadFactory());
	}

	public static Map<String, Number> getSmartBatchThreadDetails()
	  {
	    Map<String, Number> threaddetails = new HashMap();
	    threaddetails.put("active_thread_count", Integer.valueOf(SmartBatchExecutorInner.EXECUTOR.getActiveCount()));
	    threaddetails.put("used_threadpool_count", Integer.valueOf(SmartBatchExecutorInner.EXECUTOR.getPoolSize()));
	    threaddetails.put("task_count", Long.valueOf(SmartBatchExecutorInner.EXECUTOR.getTaskCount()));
	    threaddetails.put("core_pool_size", Integer.valueOf(SmartBatchExecutorInner.EXECUTOR.getCorePoolSize()));
	    threaddetails.put("largest_pool_size", Integer.valueOf(SmartBatchExecutorInner.EXECUTOR.getLargestPoolSize()));
	    threaddetails.put("maximum_pool_size", Integer.valueOf(SmartBatchExecutorInner.EXECUTOR.getMaximumPoolSize()));
	    threaddetails.put("task_queue_size", Integer.valueOf(SmartBatchExecutorInner.EXECUTOR.getQueue().size()));
	    return threaddetails;
	  }

	private static final class SmartBatchExecutorInner {

		private static final SmartBatchExecutor EXECUTOR = new SmartBatchExecutor(
				THREAD_POOL_MIN, THREAD_POOL_MAX, THREAD_POOL_KEEP_ALIVE_MS,
				MILLISECONDS, new ArrayBlockingQueue<Runnable>(
						THREAD_POOL_BACKLOG));

	}

	public static final ExecutorService getExecutor() {
		return SmartBatchExecutorInner.EXECUTOR;
	}

	/**
	 * A hook to gracefully shutdown when the JVM is shutdown
	 */
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					getExecutor().shutdownNow();
				} catch (Exception exception) {
					LOGGER.error("Exception while shutdown" + exception);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread,
	 * java.lang.Runnable)
	 */
	@Override
	protected void beforeExecute(Thread thread, Runnable runnable) {
		super.beforeExecute(thread, runnable);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable,
	 * java.lang.Throwable)
	 */
	@Override
	protected void afterExecute(Runnable runnable, Throwable throwable) {
		super.afterExecute(runnable, throwable);

	}

}