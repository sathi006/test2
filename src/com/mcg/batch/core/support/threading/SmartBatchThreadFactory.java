package com.mcg.batch.core.support.threading;

import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartBatchThreadFactory implements ThreadFactory {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchThreadFactory.class);

	@Override
	public Thread newThread(Runnable runnable) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchThreadFactory.newThread() started");
		}
		Thread thread = null;
		try {
			thread = new SmartBatchThread(runnable);
//			thread.setContextClassLoader(SmartBatchThreadFactory.class
//					.getClassLoader());
			return thread;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchThreadFactory.newThread() completed");
			}
		}
	}
}
