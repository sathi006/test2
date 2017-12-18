/**
 * 
 */
package com.mcg.batch.core;

import static com.mcg.batch.core.BatchWiringConstants.BATCH_RT_STORE_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SINGLETON;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_EXPLORER_COMPONENT;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.SimpleJobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mcg.batch.store.BatchRuntimeStore;

/**
 * 
 * This class is an implementation of {@link JobExplorer}. This class very
 * similar to {@link SimpleJobExplorer} however it uses annotation based wiring
 * and custom class loggers
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */

@Component(SMART_BATCH_EXPLORER_COMPONENT)
@Scope(SINGLETON)
public class BatchExplorer implements JobExplorer {

	@Autowired
	@Qualifier(BATCH_RT_STORE_COMPONENT)
	BatchRuntimeStore batchStore;


	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchExplorer.class);

	public List<JobExecution> getJobExecutions(JobInstance jobInstance) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.getJobExecutions() started ");
		}

		List<JobExecution> executions = this.batchStore
				.findJobExecutions(jobInstance);
		for (JobExecution jobExecution : executions) {
			getJobExecutionDependencies(jobExecution);
			for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
				getStepExecutionDependencies(stepExecution);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.getJobExecutions() completed");
		}

		return executions;
	}

	public Set<JobExecution> findRunningJobExecutions(String jobName) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.findRunningJobExecutions() started ");
		}
		Set<JobExecution> executions = this.batchStore
				.findRunningJobExecutions(jobName);
		for (JobExecution jobExecution : executions) {
			getJobExecutionDependencies(jobExecution);
			for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
				getStepExecutionDependencies(stepExecution);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.findRunningJobExecutions() completed");
		}

		return executions;
	}

	public JobExecution getJobExecution(Long executionId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.getJobExecution() started ");
		}
		if (executionId == null) {
			return null;
		}
		JobExecution jobExecution = this.batchStore
				.getJobExecution(executionId);
		if (jobExecution == null) {
			return null;
		}
		getJobExecutionDependencies(jobExecution);
		for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
			getStepExecutionDependencies(stepExecution);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.getJobExecution() completed");
		}

		return jobExecution;
	}

	public StepExecution getStepExecution(Long jobExecutionId, Long executionId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.getStepExecution() started ");
		}

		JobExecution jobExecution = this.batchStore
				.getJobExecution(jobExecutionId);
		if (jobExecution == null) {
			return null;
		}
		getJobExecutionDependencies(jobExecution);
		StepExecution stepExecution = this.batchStore.getStepExecution(
				jobExecution, executionId);
		getStepExecutionDependencies(stepExecution);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.getStepExecution() completed");
		}
		return stepExecution;
	}

	public JobInstance getJobInstance(Long instanceId) {
		return this.batchStore.getJobInstance(instanceId);
	}

	public List<JobInstance> getJobInstances(String jobName, int start,
			int count) {
		return this.batchStore.getJobInstances(jobName, start, count);
	}

	public List<String> getJobNames() {
		return this.batchStore.getJobNames();
	}

	public int getJobInstanceCount(String jobName) throws NoSuchJobException {
		return this.batchStore.getJobInstanceCount(jobName);
	}


	private void getJobExecutionDependencies(JobExecution jobExecution) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.getJobExecutionDependencies() started ");
		}
		JobInstance jobInstance = this.batchStore.getJobInstance(jobExecution);
		this.batchStore.addStepExecutions(jobExecution);
		jobExecution.setJobInstance(jobInstance);
		jobExecution.setExecutionContext(this.batchStore
				.getExecutionContext(jobExecution));

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.getJobExecutionDependencies() completed");
		}

	}

	private void getStepExecutionDependencies(StepExecution stepExecution) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.getStepExecutionDependencies() started ");
		}
		if (stepExecution != null) {
			stepExecution.setExecutionContext(this.batchStore
					.getExecutionContext(stepExecution));
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchExplorer.getStepExecutionDependencies() completed");
		}
	}
}
