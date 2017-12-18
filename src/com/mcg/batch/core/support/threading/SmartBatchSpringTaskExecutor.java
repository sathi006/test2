package com.mcg.batch.core.support.threading;

import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_TASK_EXECUTOR_COMPONENT;
import static com.mcg.batch.core.support.threading.SmartBatchExecutor.getExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component(SMART_BATCH_TASK_EXECUTOR_COMPONENT)
public class SmartBatchSpringTaskExecutor implements TaskExecutor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchSpringTaskExecutor.class);

	public SmartBatchSpringTaskExecutor() {
	}

	@Override
	public void execute(Runnable task) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchSpringTaskExecutor.execute() started");
		}
		try {
			if (getExecutor().isShutdown()) {
				LOGGER.warn("The Executor is shutdown and cannot accept anymore Tasks");
			}
			getExecutor().submit(task);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchSpringTaskExecutor.execute() completed");
			}
		}

	}

}
