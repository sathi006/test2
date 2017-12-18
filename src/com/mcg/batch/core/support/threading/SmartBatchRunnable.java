/**
 * 
 */
package com.mcg.batch.core.support.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class SmartBatchRunnable implements Runnable {
	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchRunnable.class);

	private Runnable runnable;
	private SmartBatchRuntimeContext threadContext;

	/**
	 * @param runnable
	 * @param threadContext
	 */
	public SmartBatchRunnable(Runnable runnable, SmartBatchRuntimeContext threadContext) {
		this.runnable = runnable;
		this.threadContext = threadContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRunnable.run() started");
		}
		try {
			runnable.run();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRunnable.run() completed");
			}
		}
	}

	/**
	 * @return the threadContext
	 */
	public SmartBatchRuntimeContext getThreadContext() {
		return threadContext;
	}

	/**
	 * @param threadContext
	 *            the threadContext to set
	 */
	public void setThreadContext(SmartBatchRuntimeContext threadContext) {
		this.threadContext = threadContext;
	}

}
