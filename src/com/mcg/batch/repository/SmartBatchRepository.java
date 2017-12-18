/**
 * 
 */
package com.mcg.batch.repository;

import static com.mcg.batch.core.BatchWiringConstants.BATCH_RT_STORE_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SINGLETON;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_REPOSITORY_COMPONENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.mcg.batch.store.BatchRuntimeStore;

/**
 * This class is an implementation of {@link JobRepository}. It is very similar
 * to {@link SimpleJobRepository} except the DAO objects are custom and are
 * injected using the annotations and the qualifiers pointing to components
 * defined by this framework
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 * @see {@link JobRepository} {@link SimpleJobRepository}
 */
@Component(SMART_BATCH_REPOSITORY_COMPONENT)
@Scope(SINGLETON)
public class SmartBatchRepository implements JobRepository {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchRepository.class);

	// @Autowired
	// @Qualifier(BATCH_JOBINSTANCE_DAO_COMPONENT)
	// private batchStore batchStore;
	// @Autowired
	// @Qualifier(BATCH_JOB_EXECUTION_DAO_COMPONENT)
	// private batchStore batchStore;
	// @Autowired
	// @Qualifier(BATCH_STEP_EXECUTION_DAO_COMPONENT)
	// private batchStore batchStore;
	// @Autowired
	// @Qualifier(BATCH_EXECUTION_CONTEXT_DAO_COMPONENT)
	// private ExecutionContextDao batchStore;

	@Autowired
	@Qualifier(BATCH_RT_STORE_COMPONENT)
	private BatchRuntimeStore batchStore;

	/**
	 * prevent external instantiation
	 */
	private SmartBatchRepository() {
	}

	public boolean isJobInstanceExists(final String jobName,
			final JobParameters jobParameters) {
		
			LOGGER.debug("SmartBatchRepository.isJobInstanceExists() started ");
		

		try {
			return this.batchStore.getJobInstance(jobName, jobParameters) != null;
		} finally {

			
			LOGGER.debug("SmartBatchRepository.isJobInstanceExists() completed");
			

		}

	}

	public JobExecution createJobExecution(final String jobName,
			final JobParameters jobParameters)
			throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException {


		LOGGER.debug("SmartBatchRepository.createJobExecution() started ");
		
		JobInstance jobInstance = null;
		JobExecution jobExecution = null;
		try {
			Assert.notNull(jobName, "Job name must not be null.");
			Assert.notNull(jobParameters, "JobParameters must not be null.");

			jobInstance = this.batchStore
					.getJobInstance(jobName, jobParameters);
			ExecutionContext executionContext;
			if (jobInstance != null) {
				List<JobExecution> executions = this.batchStore
						.findJobExecutions(jobInstance);

				for (JobExecution execution : executions) {
					if (execution.isRunning()) {
						throw new JobExecutionAlreadyRunningException(
								"A job execution for this job is already running: "
										+ jobInstance);
					}

					BatchStatus status = execution.getStatus();
					if ((execution.getJobParameters().getParameters().size() > 0)
							&& ((status == BatchStatus.COMPLETED) || (status == BatchStatus.ABANDONED))) {
						throw new JobInstanceAlreadyCompleteException(
								"A job instance already exists and is complete for parameters="
										+ jobParameters
										+ ".  If you want to run this job again, change the parameters.");
					}

				}

				executionContext = this.batchStore
						.getExecutionContext(this.batchStore
								.getLastJobExecution(jobInstance));
				if(LOGGER.isDebugEnabled())
	                LOGGER.debug("Using the old job instance...." +jobInstance);
			} else {
				jobInstance = this.batchStore.createJobInstance(jobName,
						jobParameters);
				executionContext = new ExecutionContext();
			}
			if(LOGGER.isDebugEnabled())
	            LOGGER.debug("The Jon ");
			jobExecution = new JobExecution(jobInstance, jobParameters, null);
			jobExecution.setExecutionContext(executionContext);
			jobExecution.setLastUpdated(new Date(System.currentTimeMillis()));

			this.batchStore.saveJobExecution(jobExecution);
			this.batchStore.saveExecutionContext(jobExecution);

			return jobExecution;
		} finally {
			jobInstance = null;
			
			LOGGER.debug("SmartBatchRepository.createJobExecution() completed");
			

		}

	}

	public void update(final JobExecution jobExecution) {

		
			LOGGER.debug("SmartBatchRepository.update() started ");
		

		try {

			Assert.notNull(jobExecution, "JobExecution cannot be null.");
			Assert.notNull(jobExecution.getJobId(),
					"JobExecution must have a Job ID set.");
			Assert.notNull(jobExecution.getId(),
					"JobExecution must be already saved (have an id assigned).");

			jobExecution.setLastUpdated(new Date(System.currentTimeMillis()));
			this.batchStore.updateJobExecution(jobExecution);
		} finally {

	
			LOGGER.debug("SmartBatchRepository.update() completed");
			

		}

	}

	public void add(final StepExecution stepExecution) {
	
		LOGGER.debug("SmartBatchRepository.add() started ");
		

		try {
			validateStepExecution(stepExecution);

			stepExecution.setLastUpdated(new Date(System.currentTimeMillis()));
			this.batchStore.saveStepExecution(stepExecution);
			this.batchStore.saveExecutionContext(stepExecution);
		} finally {

			
			LOGGER.debug("SmartBatchRepository.add() completed");
			

		}

	}

	public void addAll(final Collection<StepExecution> stepExecutions) {
		
		LOGGER.debug("SmartBatchRepository.addAll() started ");
		

		try {
			Assert.notNull(stepExecutions,
					"Attempt to save a null collection of step executions");
			for (StepExecution stepExecution : stepExecutions) {
				validateStepExecution(stepExecution);
				stepExecution.setLastUpdated(new Date(System
						.currentTimeMillis()));
			}
			this.batchStore.saveStepExecutions(stepExecutions);
			this.batchStore.saveExecutionContexts(stepExecutions);
		} finally {

			
			LOGGER.debug("SmartBatchRepository.addAll() completed");
			

		}

	}

	public void update(final StepExecution stepExecution) {
		
		LOGGER.debug("SmartBatchRepository.update() started ");
		

		try {
			validateStepExecution(stepExecution);
			Assert.notNull(stepExecution.getId(),
					"StepExecution must already be saved (have an id assigned)");

			stepExecution.setLastUpdated(new Date(System.currentTimeMillis()));
			this.batchStore.updateStepExecution(stepExecution);
			checkForInterruption(stepExecution);
		} finally {

			
			LOGGER.debug("SmartBatchRepository.update() completed");
			

		}
	}

	private void validateStepExecution(final StepExecution stepExecution) {
	
		LOGGER.debug("SmartBatchRepository.validateStepExecution() started ");
		

		Assert.notNull(stepExecution, "StepExecution cannot be null.");
		Assert.notNull(stepExecution.getStepName(),
				"StepExecution's step name cannot be null.");
		Assert.notNull(stepExecution.getJobExecutionId(),
				"StepExecution must belong to persisted JobExecution");
		
		LOGGER.debug("SmartBatchRepository.validateStepExecution() completed");
		
	}

	public void updateExecutionContext(final StepExecution stepExecution) {
		
		LOGGER.debug("SmartBatchRepository.updateExecutionContext() started ");
		
		validateStepExecution(stepExecution);
		Assert.notNull(stepExecution.getId(),
				"StepExecution must already be saved (have an id assigned)");
		this.batchStore.updateExecutionContext(stepExecution);

		
		LOGGER.debug("SmartBatchRepository.updateExecutionContext() completed");
		

	}

	public void updateExecutionContext(final JobExecution jobExecution) {
		
		LOGGER.debug("SmartBatchRepository.updateExecutionContext() started ");
		
		this.batchStore.updateExecutionContext(jobExecution);

		
		LOGGER.debug("SmartBatchRepository.updateExecutionContext() completed");
		

	}

	public StepExecution getLastStepExecution(final JobInstance jobInstance,
			final String stepName) {

	
		LOGGER.debug("SmartBatchRepository.getLastStepExecution() started ");
		
		List<JobExecution> jobExecutions = this.batchStore
				.findJobExecutions(jobInstance);
		List<StepExecution> stepExecutions = new ArrayList<StepExecution>(
				jobExecutions.size());

		for (JobExecution jobExecution : jobExecutions) {
			this.batchStore.addStepExecutions(jobExecution);
			for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
				if (stepName.equals(stepExecution.getStepName())) {
					stepExecutions.add(stepExecution);
				}
			}
		}

		StepExecution latest = null;
		for (StepExecution stepExecution : stepExecutions) {
			if (latest == null) {
				latest = stepExecution;
			}
			if (latest.getStartTime().getTime() < stepExecution.getStartTime()
					.getTime()) {
				latest = stepExecution;
			}
		}

		if (latest != null) {
			ExecutionContext executionContext = this.batchStore
					.getExecutionContext(latest);
			latest.setExecutionContext(executionContext);
		}

		
		LOGGER.debug("SmartBatchRepository.getLastStepExecution() completed");
		

		return latest;
	}

	public int getStepExecutionCount(final JobInstance jobInstance,
			final String stepName) {
		
		LOGGER.debug("SmartBatchRepository.getStepExecutionCount() started ");
		
		int count = 0;
		List<JobExecution> jobExecutions = this.batchStore
				.findJobExecutions(jobInstance);
		for (JobExecution jobExecution : jobExecutions) {
			this.batchStore.addStepExecutions(jobExecution);
			for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
				if (stepName.equals(stepExecution.getStepName())) {
					count++;
				}
			}
		}

		
		LOGGER.debug("SmartBatchRepository.getStepExecutionCount() completed");
		

		return count;
	}

	private void checkForInterruption(final StepExecution stepExecution) {

		
		LOGGER.debug("SmartBatchRepository.checkForInterruption() started ");
		

		JobExecution jobExecution = stepExecution.getJobExecution();
		this.batchStore.synchronizeStatus(jobExecution);
		if (jobExecution.isStopping()) {
			LOGGER.info("Parent JobExecution is stopped, so passing message on to StepExecution");
			stepExecution.setTerminateOnly();
		}
	
		LOGGER.debug("SmartBatchRepository.checkForInterruption() completed");
		
	}

	public JobExecution getLastJobExecution(final String jobName,
			final JobParameters jobParameters) {
	
		LOGGER.debug("SmartBatchRepository.getLastJobExecution() started ");
		

		JobInstance jobInstance = this.batchStore.getJobInstance(jobName,
				jobParameters);
		if (jobInstance == null) {
			return null;
		}
		JobExecution jobExecution = this.batchStore
				.getLastJobExecution(jobInstance);

		if (jobExecution != null) {
			jobExecution.setExecutionContext(this.batchStore
					.getExecutionContext(jobExecution));
		}

		
		LOGGER.debug("SmartBatchRepository.getLastJobExecution() completed");
		

		return jobExecution;
	}

	public JobInstance createJobInstance(final String jobName,
			final JobParameters jobParameters) {

		
		LOGGER.debug("SmartBatchRepository.createJobInstance() started ");
		
		Assert.notNull(jobName,
				"A job name is required to create a JobInstance");
		Assert.notNull(jobParameters,
				"Job parameters are required to create a JobInstance");

		JobInstance jobInstance = this.batchStore.createJobInstance(jobName,
				jobParameters);

		
		LOGGER.debug("SmartBatchRepository.createJobInstance() completed");
		

		return jobInstance;
	}

	public JobExecution createJobExecution(final JobInstance jobInstance,
			JobParameters jobParameters, String jobConfigurationLocation) {
		
		LOGGER.debug("SmartBatchRepository.createJobExecution() started ");
		
		Assert.notNull(jobInstance,
				"A JobInstance is required to associate the JobExecution with");
		Assert.notNull(jobParameters,
				"A JobParameters object is required to create a JobExecution");

		JobExecution jobExecution = new JobExecution(jobInstance,
				jobParameters, jobConfigurationLocation);
		ExecutionContext executionContext = new ExecutionContext();
		jobExecution.setExecutionContext(executionContext);
		jobExecution.setLastUpdated(new Date(System.currentTimeMillis()));

		this.batchStore.saveJobExecution(jobExecution);
		this.batchStore.saveExecutionContext(jobExecution);

		
		LOGGER.debug("SmartBatchRepository.createJobExecution() completed");
		

		return jobExecution;
	}
}