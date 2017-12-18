/**
 * 
 */
package com.mcg.batch.core.support.lifecycle.listeners;

import static com.mcg.batch.core.BatchWiringConstants.BATCH_LIFECYCLE_LISTENER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_RT_STORE_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.PROTOTYPE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mcg.batch.store.BatchRuntimeStore;

/**
 * Implementations for updating the jobExecutionLifecycle in the BatchStore
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@Component(BATCH_LIFECYCLE_LISTENER_COMPONENT)
@Scope(PROTOTYPE)
public class BatchLifeCycleListener implements JobExecutionListener {
	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchLifeCycleListener.class);

	/**
	 * batch Store
	 */
	@Autowired
	@Qualifier(BATCH_RT_STORE_COMPONENT)
	BatchRuntimeStore batchStore;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.JobExecutionListener#beforeJob(org.
	 * springframework.batch.core.JobExecution)
	 */
	@Override
	public void beforeJob(final JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchLifeCycleListener.beforeJob() started");
		}
		try {
			batchStore.linkNodeStartedJobs(jobExecution.getId());
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchLifeCycleListener.beforeJob() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.JobExecutionListener#afterJob(org.
	 * springframework.batch.core.JobExecution)
	 */
	@Override
	public void afterJob(final JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchLifeCycleListener.afterJob() started");
		}
		try {
			batchStore.deLinkNodeStartedJobs(jobExecution.getId());
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchLifeCycleListener.afterJob() completed");
			}
		}
	}

}
