/**
 * 
 */
package com.mcg.batch.core;

import static com.mcg.batch.core.BatchConfiguration.BATCH_RESTARTABLE_AGE;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_LAUNCHER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_META_STORE_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_EXPLORER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_OPERATOR_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_REGISTRY_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_REPOSITORY_COMPONENT;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.support.PropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.mcg.batch.core.support.BatchDefinitionBean;
import com.mcg.batch.store.BatchMetadataStore;
import com.mcg.batch.utils.SmartBatchJobParametersConvertor;
import com.mcg.batch.utils.StringHelper;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@Component(SMART_BATCH_OPERATOR_COMPONENT)
public class BatchOperator implements JobOperator {
	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchOperator.class);

	/**
	 * jobRegistry.
	 */
	@Autowired(required = true)
	@Qualifier(SMART_BATCH_REGISTRY_COMPONENT)
	private JobRegistry jobRegistry;

	/**
	 * jobExplorer.
	 */
	@Autowired(required = true)
	@Qualifier(SMART_BATCH_EXPLORER_COMPONENT)
	private BatchExplorer jobExplorer;

	/**
	 * jobLauncher.
	 */
	@Autowired(required = true)
	@Qualifier(BATCH_LAUNCHER_COMPONENT)
	private BatchLauncher jobLauncher;

	/**
	 * jobRepository.
	 */
	@Autowired(required = true)
	@Qualifier(SMART_BATCH_REPOSITORY_COMPONENT)
	private JobRepository jobRepository;
	
	@Autowired
	@Qualifier(BATCH_META_STORE_COMPONENT)
	BatchMetadataStore batchMetaStore;

	/**
	 *
	 * @throws Exception
	 *             Exception
	 */
	public final void afterPropertiesSet() throws Exception {
		Assert.notNull(this.jobLauncher, "JobLauncher must be provided");
		Assert.notNull(this.jobRegistry, "JobLocator must be provided");
		Assert.notNull(this.jobExplorer, "JobExplorer must be provided");
		Assert.notNull(this.jobRepository, "JobRepository must be provided");
	}

	/**
	 * <p>
	 * This method will return the list of job execution Id for a given Instance
	 * Id.
	 *
	 * @param instanceId
	 *            Long
	 * @return List<Long>
	 *
	 */
	public final List<Long> getExecutions(final long instanceId) {
		JobInstance jobInstance = this.jobExplorer.getJobInstance(Long
				.valueOf(instanceId));
		if (jobInstance == null) {
			try {
				throw new NoSuchJobInstanceException(String.format(
						"No job instance with id=%d",
						new Object[] { Long.valueOf(instanceId) }));
			} catch (NoSuchJobInstanceException e) {
				LOGGER.error("No Such instance is available ", e);
			}
		}
		List<Long> list = new ArrayList<Long>();
		for (JobExecution jobExecution : this.jobExplorer
				.getJobExecutions(jobInstance)) {
			list.add(jobExecution.getId());
		}
		return list;
	}

	/**
	 * <p>
	 * This method will return the list of job names.
	 *
	 * @return Set<String>
	 *
	 */

	public final Set<String> getJobNames() {
		return new TreeSet<String>(this.jobRegistry.getJobNames());
	}

	/**
	 * <p>
	 * This method will return the list of Job instance id for a given jobName.
	 *
	 * @param jobName
	 *            String
	 * @param start
	 *            Integer
	 * @param count
	 *            Integer
	 * @return List<Long>
	 * @throws NoSuchJobException
	 *             Exception
	 */

	public final List<Long> getJobInstances(final String jobName,
			final int start, final int count) throws NoSuchJobException {
		List<Long> list = new ArrayList<Long>();
		for (JobInstance jobInstance : this.jobExplorer.getJobInstances(
				jobName, start, count)) {
			list.add(jobInstance.getId());
		}
		if (list.isEmpty() && !this.jobRegistry.getJobNames().contains(jobName)) {
			throw new NoSuchJobException("No such job (either in"
					+ " registry or in historical data): " + jobName);
		}
		return list;
	}

	/**
	 * <p>
	 * This method will return the parameters for a given executionId.
	 *
	 * @param executionId
	 *            Long
	 * @return String - parameters
	 * @throws NoSuchJobExecutionException
	 *             Exception
	 *
	 */

	public final String getParameters(final long executionId)
			throws NoSuchJobExecutionException {
		JobExecution jobExecution = findExecutionById(executionId);

		return PropertiesConverter
				.propertiesToString(new DefaultJobParametersConverter()
						.getProperties(jobExecution.getJobParameters()));
	}

	/**
	 * <p>
	 * This method will return the list of running batch executions.
	 *
	 * @param jobName
	 *            String
	 * @return Set<Long>
	 * @throws NoSuchJobException
	 *             Exception
	 */

	public final Set<Long> getRunningExecutions(final String jobName)
			throws NoSuchJobException {
		Set<Long> set = new LinkedHashSet<Long>();
		for (JobExecution jobExecution : this.jobExplorer
				.findRunningJobExecutions(jobName)) {
			set.add(jobExecution.getId());
		}
		if (set.isEmpty() && !this.jobRegistry.getJobNames().contains(jobName)) {
			throw new NoSuchJobException("No such job (either"
					+ " in registry or in historical data): " + jobName);
		}
		return set;
	}

	/**
	 * <P>
	 * This method returns a list of step execution summaries.
	 *
	 * @param executionId
	 *            Long
	 * @return Map<Long, String>
	 * @throws NoSuchJobExecutionException
	 *             Exception
	 */

	public final Map<Long, String> getStepExecutionSummaries(
			final long executionId) throws NoSuchJobExecutionException {
		JobExecution jobExecution = findExecutionById(executionId);

		Map<Long, String> map = new LinkedHashMap<Long, String>();
		for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
			map.put(stepExecution.getId(), stepExecution.toString());
		}
		return map;
	}

	/**
	 * <p>
	 * This method return the summary of a given executionId.
	 *
	 * @param executionId
	 *            Long
	 * @return String - summary
	 * @throws NoSuchJobExecutionException
	 *             Exception
	 */

	public final String getSummary(final long executionId)
			throws NoSuchJobExecutionException {
		JobExecution jobExecution = findExecutionById(executionId);
		return jobExecution.toString();
	}

	/**
	 * <p>
	 * This method will restart a failed or suspended batch instance.
	 *
	 * @param executionId
	 *            Long
	 * @return Long - batch execution Id
	 * @throws NoSuchJobException
	 *             Exception
	 * @throws NoSuchJobExecutionException
	 *             Exception
	 * @throws JobRestartException
	 *             Exception
	 * @throws JobInstanceAlreadyCompleteException
	 *             Exception
	 * @throws JobParametersInvalidException
	 *             Exception
	 */

	public final Long restart(final long executionId)
			throws NoSuchJobException, NoSuchJobExecutionException,
			JobRestartException, JobInstanceAlreadyCompleteException,
			JobParametersInvalidException {
		LOGGER.info("Checking status of job execution with" + " id="
				+ executionId);
	
		JobExecution jobExecution = findExecutionById(executionId);
		Long lastUpdatedTime = jobExecution.getLastUpdated().getTime();
		Long currentTime = System.currentTimeMillis();
		Long minRestartableAge = 30L;
		
		BatchDefinitionBean batchDefinitionBean = batchMetaStore.getBatchDefniton(jobExecution.getJobInstance().getJobName());
		String value = batchDefinitionBean.getParam(BATCH_RESTARTABLE_AGE,
				String.class);
		
		if (value != null && value != "") {
			minRestartableAge = Long.parseLong(value);
		}
		
		minRestartableAge  = minRestartableAge * 86400000L;
	
		if((currentTime - lastUpdatedTime)  <= minRestartableAge){
		
		String jobName = jobExecution.getJobInstance().getJobName();
		Job job = this.jobRegistry.getJob(jobName);
		JobParameters parameters = jobExecution.getJobParameters();
		
		LOGGER.info(String.format("Attempting to resume job"
				+ " with name=%s and parameters=%s", new Object[] { jobName,
				parameters }));
		try {
		    	jobExecution = this.jobLauncher.run(job, parameters);
			return jobExecution.getId();
		}
		 catch (JobExecutionAlreadyRunningException e) {
			throw new UnexpectedJobExecutionException(String.format(
					"Illegal state (only happens on a race"
							+ " condition): %s with name=%s and"
							+ " parameters=%s", new Object[] {
							"job" + " execution already running", jobName,
							parameters }), e);
		}
		}
		else{
			if(LOGGER.isErrorEnabled()){
			 
			  LOGGER.error("Could not able to restart execution as its last executed time is greater than configured time limit");
			}
			throw new JobRestartException("Could not able to restart execution as its last executed time is greater than configured time limit");
		}
		
		
		
		
	}

	/**
	 *
	 * @param executionId
	 *            String
	 * @return jobExecution JobExecution
	 * @throws JobRestartException
	 *             JobRestartException
	 * @throws JobInstanceAlreadyCompleteException
	 *             JobInstanceAlreadyCompleteException
	 * @throws JobParametersInvalidException
	 *             JobParametersInvalidException
	 * @throws NoSuchJobExecutionException
	 *             NoSuchJobExecutionException
	 * @throws NoSuchJobException
	 *             NoSuchJobException
	 */
	public final JobExecution restartExecution(final long executionId)
			throws JobRestartException, JobInstanceAlreadyCompleteException,
			JobParametersInvalidException, NoSuchJobExecutionException,
			NoSuchJobException {
		LOGGER.info("Checking status of job execution with" + " id="
				+ executionId);

		JobExecution jobExecution = findExecutionById(executionId);

		String jobName = jobExecution.getJobInstance().getJobName();
		Job job = this.jobRegistry.getJob(jobName);
		JobParameters parameters = jobExecution.getJobParameters();

		LOGGER.info(String.format("Attempting to resume job"
				+ " with name=%s and parameters=%s", new Object[] { jobName,
				parameters }));
		try {
			jobExecution = this.jobLauncher.run(job, parameters);
			return jobExecution;
		} catch (JobExecutionAlreadyRunningException e) {
			throw new UnexpectedJobExecutionException(String.format(
					"Illegal state (only happens on a race"
							+ " condition): %s with name=%s and"
							+ " parameters=%s", new Object[] {
							"job" + " execution already running", jobName,
							parameters }), e);
		}
	}

	/**
	 * <P>
	 * This method will start a jobInstance for a given JobName and parameters.
	 *
	 * @param jobName
	 *            String
	 * @param parameters
	 *            String
	 * @return Long
	 * @throws NoSuchJobException
	 *             Exception
	 * @throws JobInstanceAlreadyExistsException
	 *             Exception
	 * @throws JobParametersInvalidException
	 *             Exception
	 */

	public final Long start(final String jobName, final String parameters)
			throws NoSuchJobException, JobInstanceAlreadyExistsException,
			JobParametersInvalidException {
		LOGGER.info("Checking status of job with name=" + jobName);

		JobParameters jobParameters = new SmartBatchJobParametersConvertor()
				.getJobParameters(StringHelper
						.delimitedStringsToProperty(parameters));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("The job parameters are ..."+jobParameters);
		}
		if (this.jobRepository.isJobInstanceExists(jobName, jobParameters)) {
			throw new JobInstanceAlreadyExistsException(String.format(
					"Cannot start a job instance that already"
							+ " exists with name=%s and parameters=%s",
					new Object[] { jobName, parameters }));
		}

		Job job = this.jobRegistry.getJob(jobName);

		LOGGER.info(String.format("Attempting to launch job"
				+ " with name=%s and parameters=%s", new Object[] { jobName,
				parameters }));
		try {
			return this.jobLauncher.run(job, jobParameters).getId();
		} catch (JobExecutionAlreadyRunningException e) {
			throw new UnexpectedJobExecutionException(String.format(
					"Illegal state (only happens on"
							+ " a race condition): %s with name=%s and"
							+ " parameters=%s", new Object[] {
							"job execution" + " already running", jobName,
							parameters }), e);
		} catch (JobRestartException e) {
			throw new UnexpectedJobExecutionException(String.format(
					"Illegal state (only happens on"
							+ " a race condition): %s with name=%s and"
							+ " parameters=%s", new Object[] {
							"job not" + " restartable", jobName, parameters }),
					e);
		} catch (JobInstanceAlreadyCompleteException e) {
			throw new UnexpectedJobExecutionException(
					String.format("Illegal state (only happens on a race"
							+ " condition): %s with name=%s and parameters=%s",
							new Object[] { "job already complete", jobName,
									parameters }), e);
		}
	}

	/**
	 * <p>
	 * This method starts a new instance of a batch. It increments the job
	 * parameter by adding a run id to differ from the same batch parameter.
	 *
	 * @param jobName
	 *            String
	 * @return Long
	 * @throws NoSuchJobException
	 *             Exception
	 * @throws JobParametersNotFoundException
	 *             Exception
	 * @throws JobParametersInvalidException
	 *             Exception
	 */

	public final Long startNextInstance(final String jobName)
			throws NoSuchJobException, JobParametersNotFoundException,
			JobParametersInvalidException {
		LOGGER.info("Locating parameters for next instance" 
				+ " of job with name=" + jobName);

		Job job = this.jobRegistry.getJob(jobName);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Job  aquired from registry" + job);
		}
		List<JobInstance> lastInstances = this.jobExplorer.getJobInstances(
				jobName, 0, 1);
		JobParameters newJobParameters = new JobParametersBuilder().addLong(
				"current.TimeStamp", System.currentTimeMillis())
				.toJobParameters();
		BatchParameterIncrementer incrementer = (BatchParameterIncrementer) job
				.getJobParametersIncrementer();

		if (incrementer == null) {
			incrementer = new BatchParameterIncrementer();
			// throw new JobParametersNotFoundException("No job"
			// + " parameters incrementer found for job=" + jobName);
		}

		JobParameters parameters;

		if (lastInstances.isEmpty()) {
			parameters = incrementer.getNext(newJobParameters);
			if (parameters == null) {
				throw new JobParametersNotFoundException("No"
						+ " bootstrap parameters found for" + " job=" + jobName);
			}

		} else {

			List<JobExecution> lastExecutions = this.jobExplorer
					.getJobExecutions((JobInstance) lastInstances.get(0));
			if (lastExecutions != null && lastExecutions.size() > 0) {
				parameters = ((JobExecution) lastExecutions.get(0))
						.getJobParameters();
				if (parameters.equals(newJobParameters)) {
					parameters = incrementer.getNext(parameters);
				} else {
					parameters = incrementer.getNext(newJobParameters);
				}
			} else {
				parameters = incrementer.getNext(newJobParameters);
			}
			LOGGER.info("BatchOperator.startNextInstance()" + parameters);
		}

		LOGGER.info(String.format("Attempting to launch"
				+ " job with name=%s and parameters=%s", new Object[] {
				jobName, parameters }));
		try {
			return this.jobLauncher.run(job, parameters).getId();
		} catch (JobExecutionAlreadyRunningException e) {
			throw new UnexpectedJobExecutionException(
					String.format("Illegal state (only happens on a race"
							+ " condition): %s with name=%s and parameters=%s",
							new Object[] { "job already running", jobName,
									parameters }), e);
		} catch (JobRestartException e) {
			throw new UnexpectedJobExecutionException(String.format(
					"Illegal state (only happens"
							+ " on a race condition): %s with name=%s"
							+ " and parameters=%s", new Object[] {
							"job not restartable", jobName, parameters }), e);
		} catch (JobInstanceAlreadyCompleteException e) {
			throw new UnexpectedJobExecutionException(String.format(
					"Illegal state (only happens on a race"
							+ " condition): %s with name=%s and"
							+ " parameters=%s", new Object[] {
							"job instance already complete", jobName,
							parameters }), e);
		}
	}

	/**
	 * <p>
	 * This method stops a JobExecution for a given executionId.
	 *
	 * @param executionId
	 *            Long
	 * @return boolean - stop status
	 * @throws NoSuchJobExecutionException
	 *             Exception
	 * @throws JobExecutionNotRunningException
	 *             Exception
	 */
	@Transactional
	public final boolean stop(final long executionId)
			throws NoSuchJobExecutionException, JobExecutionNotRunningException {
		JobExecution jobExecution = findExecutionById(executionId);
		Job job;
		BatchStatus status = jobExecution.getStatus();
		if (status != BatchStatus.STARTED && status != BatchStatus.STARTING) {
			throw new JobExecutionNotRunningException("JobExecution "
					+ "must be running so that " + "it can be stopped: "
					+ jobExecution);
		}
		jobExecution.setStatus(BatchStatus.STOPPING);
		this.jobRepository.update(jobExecution);
		try {
			job = this.jobRegistry.getJob(jobExecution.getJobInstance()
					.getJobName());
			if (job instanceof StepLocator) {
				for (StepExecution stepExecution : jobExecution
						.getStepExecutions()) {
					Step step = ((StepLocator) job).getStep(stepExecution
							.getStepName());
					if (stepExecution.getStatus().isRunning()
							&& step instanceof TaskletStep) {
						try {
							Tasklet tasklet = ((TaskletStep) step).getTasklet();
							if (tasklet instanceof StoppableTasklet) {
								((StoppableTasklet) tasklet).stop();
							}
						} catch (Exception e) {
							if (e instanceof NoSuchStepException) {
								LOGGER.warn("Step not found", e);
							} else if (e instanceof JobInterruptedException) {
								LOGGER.debug("Handled the Interrupted Exception");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			if (e instanceof NoSuchJobException) {
				LOGGER.warn("Cannot find Job object", e);
			} else if (e instanceof JobInterruptedException) {
				LOGGER.debug("Handled the Interrupted Exception");
			}
		}
		return true;
	}

	/**
	 * <p>
	 * This method abandon a JobExecution for given executionId.
	 *
	 * @param jobExecutionId
	 *            Long
	 * @return JobExecution
	 * @throws NoSuchJobExecutionException
	 *             Exception
	 * @throws JobExecutionAlreadyRunningException
	 *             Exception
	 */

	public final JobExecution abandon(final long jobExecutionId)
			throws NoSuchJobExecutionException,
			JobExecutionAlreadyRunningException {
		JobExecution jobExecution = findExecutionById(jobExecutionId);
		if (jobExecution.getStatus().isLessThan(BatchStatus.STOPPING)) {
			throw new JobExecutionAlreadyRunningException(""
					+ "JobExecution is running or complete and"
					+ " therefore cannot be aborted");
		}

		jobExecution.upgradeStatus(BatchStatus.ABANDONED);
		jobExecution.setEndTime(new Date());
		this.jobRepository.update(jobExecution);

		return jobExecution;
	}

	/**
	 * <p>
	 * This method returns a JobExecution for a given executionId.
	 *
	 * @param executionId
	 *            Long
	 * @return JobExecution
	 * @throws NoSuchJobExecutionException
	 *             Exception
	 */

	private JobExecution findExecutionById(final long executionId)
			throws NoSuchJobExecutionException {
		JobExecution jobExecution = this.jobExplorer.getJobExecution(Long
				.valueOf(executionId));
		if (jobExecution == null) {
			throw new NoSuchJobExecutionException("No"
					+ " JobExecution found for id: [" + executionId + "]");
		}
		return jobExecution;
	}

}
