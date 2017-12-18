package com.mcg.batch.core.support.threading;

import static com.mcg.batch.utils.StringHelper.concat;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//Referenced classes of package com.mcg.batch.core.support.threading:
//SmartBatchExecutor


 public class SmartBatchThread extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmartBatchThread.class); 
	
	private static final AtomicLong THREAD_COUNT = new AtomicLong();
	private static final String THREAD_PREFIX = "smart-batch-thread-";

	SmartBatchThread(Runnable runnable) {
		super(runnable, concat(THREAD_PREFIX, THREAD_COUNT.incrementAndGet()));
		java.util.Map threaddetails = SmartBatchExecutor.getSmartBatchThreadDetails();
		LOGGER.debug("SmartBatchThreadDetails:" + threaddetails);
	}
	
 

	public SmartBatchThread() {
		super(concat(THREAD_PREFIX, THREAD_COUNT.incrementAndGet()));
	}

}
