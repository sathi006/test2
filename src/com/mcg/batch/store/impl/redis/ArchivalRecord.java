/**
 * 
 */
package com.mcg.batch.store.impl.redis;

import java.io.Serializable;
import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class ArchivalRecord implements Serializable {

	/**
	 * Auto Generated serialVersionUID
	 */
	private static final long serialVersionUID = -1913360304795784423L;
	private JobInstance jobInstance;
	private JobExecution jobExecution;
	private List<StepExecution> stepExecutions;
	private ExecutionContext jobExecutionContext;
	private List<ExecutionContext> stepExecutionContext;
	private JobParameters jobParameters;

	/**
	 * @param jobInstance
	 * @param jobExecution
	 * @param stepExecutions
	 * @param jobExecutionContext
	 * @param stepExecutionContext
	 * @param jobParameters
	 */
	public ArchivalRecord(JobInstance jobInstance, JobExecution jobExecution,
			List<StepExecution> stepExecutions,
			ExecutionContext jobExecutionContext,
			List<ExecutionContext> stepExecutionContext,
			JobParameters jobParameters) {
		this.jobInstance = jobInstance;
		this.jobExecution = jobExecution;
		this.stepExecutions = stepExecutions;
		this.jobExecutionContext = jobExecutionContext;
		this.stepExecutionContext = stepExecutionContext;
		this.jobParameters = jobParameters;
	}

	/**
	 * @return the jobInstance JobInstance
	 */
	public JobInstance getJobInstance() {
		return jobInstance;
	}

	/**
	 * @return the jobExecution JobExecution
	 */
	public JobExecution getJobExecution() {
		return jobExecution;
	}

	/**
	 * @return the stepExecutions List<StepExecution>
	 */
	public List<StepExecution> getStepExecutions() {
		return stepExecutions;
	}

	/**
	 * @return the jobExecutionContext ExecutionContext
	 */
	public ExecutionContext getJobExecutionContext() {
		return jobExecutionContext;
	}

	/**
	 * @return the stepExecutionContext List<ExecutionContext>
	 */
	public List<ExecutionContext> getStepExecutionContext() {
		return stepExecutionContext;
	}

	/**
	 * @return the jobParameters JobParameters
	 */
	public JobParameters getJobParameters() {
		return jobParameters;
	}

}