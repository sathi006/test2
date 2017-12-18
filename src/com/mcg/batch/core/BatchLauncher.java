/**
 * 
 */
package com.mcg.batch.core;

import static com.mcg.batch.core.BatchWiringConstants.BATCH_LAUNCHER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SINGLETON;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_REPOSITORY_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_TASK_EXECUTOR_COMPONENT;

import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.mcg.batch.core.support.threading.SmartBatchRuntimeContext;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@Component(BATCH_LAUNCHER_COMPONENT)
@Scope(SINGLETON)
public class BatchLauncher implements JobLauncher {
	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchLauncher.class);

	@Autowired(required = true)
	@Qualifier(SMART_BATCH_REPOSITORY_COMPONENT)
	private JobRepository jobRepository;

	@Autowired
	@Qualifier(SMART_BATCH_TASK_EXECUTOR_COMPONENT)
	private TaskExecutor taskExecutor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.launch.JobLauncher#run(org.springframework
	 * .batch.core.Job, org.springframework.batch.core.JobParameters)
	 */
	@Override
	public JobExecution run(final Job job, final JobParameters jobParameters)
			throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchLauncher.run() started ");
		}
		Assert.notNull(job, "The Job must not be null.");
		Assert.notNull(jobParameters, "The JobParameters must not be null.");

		final JobExecution lastInstance = this.jobRepository.getLastJobExecution(
				job.getName(), jobParameters);
		if (lastInstance != null) {
			if (!job.isRestartable()) {
				throw new JobRestartException(
						"JobInstance already exists and is not restartable");
			}

			for (StepExecution execution : lastInstance.getStepExecutions()) {
				if (execution.getStatus() == BatchStatus.UNKNOWN) {
					throw new JobRestartException("Step ["
							+ execution.getStepName()
							+ "] is of status UNKNOWN");
				}
			}
		}

		job.getJobParametersValidator().validate(jobParameters);

		final JobExecution jobExecution = this.jobRepository
				.createJobExecution(job.getName(), jobParameters);
		final String namespace = ThreadContextUtils.getNamespace();
		
		taskExecutor.execute(new Runnable() {
			// Runnable runnable= new Runnable() {

			@Override
			public void run() {
				try {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("The current Thread id "+ Thread.currentThread().getName());
					}
					ThreadContextUtils
							.setRunTimeContext(new SmartBatchRuntimeContext(
									namespace, jobExecution.getId(),
									jobExecution.getJobInstance().getId()));
					copyFailedContextToThreadContext(lastInstance);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("The job is created .."+job);
						
					}
					job.execute(jobExecution);
				} catch (UnexpectedJobExecutionException ex) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("BatchLauncher.run(...).new Runnable() {...}.run()");
					}
					ex.printStackTrace();
					if (jobExecution.getExitStatus().compareTo(
							ExitStatus.UNKNOWN) == 0
							|| jobExecution.getStatus().equals(
									BatchStatus.UNKNOWN)) {
						jobExecution.setStatus(BatchStatus.FAILED);
						jobExecution.setExitStatus(ExitStatus.FAILED
								.addExitDescription(ex));
					} else if (jobExecution.getExitStatus().compareTo(
							ExitStatus.FAILED) == 0
							|| jobExecution.getStatus().equals(
									BatchStatus.FAILED)) {
						ExitStatus currentStatus = jobExecution.getExitStatus();
						List<StepExecution> stepExecutions = (List<StepExecution>) jobExecution
								.getStepExecutions();
						for (StepExecution s : stepExecutions) {
							if (s.getExitStatus().compareTo(ExitStatus.FAILED) == 0
									|| s.getExitStatus().equals(
											ExitStatus.FAILED)) {
								jobExecution.setExitStatus(currentStatus
										.addExitDescription(s.getExitStatus()
												.getExitDescription()));
								break;
							}
						}
					}
					BatchLauncher.this.jobRepository.update(jobExecution);
				} catch (Exception e) {

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(
								"BatchLauncher.run(...).new Runnable() {...}.run()",
								e);
					}
					if (jobExecution.getExitStatus().compareTo(
							ExitStatus.UNKNOWN) == 0
							|| jobExecution.getStatus().equals(
									BatchStatus.UNKNOWN)) {
						jobExecution.setStatus(BatchStatus.FAILED);
						jobExecution.setExitStatus(ExitStatus.FAILED
								.addExitDescription(e));
					} else if (jobExecution.getExitStatus().compareTo(
							ExitStatus.FAILED) == 0
							|| jobExecution.getStatus().equals(
									BatchStatus.FAILED)) {
						ExitStatus currentStatus = jobExecution.getExitStatus();
						List<StepExecution> stepExecutions = (List<StepExecution>) jobExecution
								.getStepExecutions();
						for (StepExecution s : stepExecutions) {
							if (s.getExitStatus().compareTo(ExitStatus.FAILED) == 0
									|| s.getExitStatus().equals(
											ExitStatus.FAILED)) {
								jobExecution.setExitStatus(currentStatus
										.addExitDescription(s.getExitStatus()
												.getExitDescription()));
								break;
							}
						}
					}
					BatchLauncher.this.jobRepository.update(jobExecution);
				} finally {
					ThreadContextUtils.clear();
				}
			}
		});
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchLauncher.run() completed");
		}
		return jobExecution;
	}
	
	private void copyFailedContextToThreadContext(final JobExecution jobExecution) {
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("BatchOperator.findFailedStepExecution Started");
	    }
	    ExecutionContext stepContext = null;
	    try {
		    if (jobExecution != null) {
			List<StepExecution> stepExecutions = (List<StepExecution>) jobExecution
				.getStepExecutions();
			if (stepExecutions != null) {
			    for (StepExecution execution : stepExecutions) {
				if (ExitStatus.FAILED.compareTo(execution.getExitStatus()) == 0
					|| BatchStatus.FAILED.equals(execution.getStatus())) {
				stepContext = execution.getExecutionContext();
				if (stepContext != null) {
				  for (Entry<String, Object> setEntry : stepContext.entrySet()) {
				      if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Copying Context Key : " + setEntry.getKey() + " Value : " + setEntry.getValue() + " to Execution Context");
				    }
				      ThreadContextUtils.addToExecutionContext(setEntry.getKey(), setEntry.getValue());
				  }
				} else {
				    if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("No Execution Context found for Step : " + execution.getStepName());
				    }
				}
			    }
			}
		    }
		    }
	    } catch (Exception e) {
		LOGGER.warn("Context form Failed step could not be retrieved due to exception : ", e);
	    } finally {
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("BatchOperator.findFailedStepExecution Finished");
		}
	    }
	}
}
