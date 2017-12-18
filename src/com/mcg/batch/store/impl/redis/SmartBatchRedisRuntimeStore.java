/**
 * 
 */
package com.mcg.batch.store.impl.redis;

import static com.mcg.batch.core.BatchConfiguration.BATCH_EXECUTION_WARNINGS;
import static com.mcg.batch.core.BatchConfiguration.NODE_NAME;
import static com.mcg.batch.core.BatchConfiguration.TIMESTAMP_FORMAT;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_META_STORE_COMPONENT;
import static com.mcg.batch.store.ArchivalInstanceDetails.ARCHIVER_DETAILS;
import static com.mcg.batch.utils.StringHelper.HASH_CHAR;
import static com.mcg.batch.utils.StringHelper.HYPHEN_CHAR;
import static com.mcg.batch.utils.StringHelper.NEVER;
import static com.mcg.batch.utils.StringHelper.concat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.batch.runtime.BatchStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.DefaultJobKeyGenerator;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobKeyGenerator;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.util.Assert;

import com.mcg.batch.cache.SmartBatchCache;
import com.mcg.batch.cache.SmartBatchCacheFactory;
import com.mcg.batch.cache.impl.RedisCacheFactory;
import com.mcg.batch.core.support.BatchDefinitionBean;
import com.mcg.batch.core.support.threading.SmartBatchExecutor;
import com.mcg.batch.exceptions.BatchException;
import com.mcg.batch.store.ArchivalInstanceDetails;
import com.mcg.batch.store.Archiver;
import com.mcg.batch.store.BatchMetadataStore;
import com.mcg.batch.store.BatchRuntimeStore;
import com.mcg.batch.utils.CollectionUtils;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * This class is an implementation of {@link BatchRuntimeStore} interface.
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class SmartBatchRedisRuntimeStore implements BatchRuntimeStore {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchRedisRuntimeStore.class);

	private static final SmartBatchCacheFactory CACHE_FACTORY = RedisCacheFactory
			.getInstance();

	/**
	 * batch runtime store
	 */
	@Autowired
	@Qualifier(BATCH_META_STORE_COMPONENT)
	BatchMetadataStore batchMetaStore;

	/**
	 * Batch JOB Context Cache Key.
	 */
	public static final String BATCH_JOB_EXECUTION_CONTEXT = "BATCH_JOB_EXECUTION_CONTEXT";
	/**
	 * Batch Step Context Cache Key.
	 */
	public static final String BATCH_STEP_EXECUTION_CONTEXT = "BATCH_STEP_EXECUTION_CONTEXT";
	/**
	 * Batch Step Execution Cache Key.
	 */
	public static final String BATCH_STEP_EXECUTION = "BATCH_STEP_EXECUTION";
	/**
	 * Batch Job Execution Cache Key.
	 */
	public static final String BATCH_JOB_EXECUTION = "BATCH_JOB_EXECUTION";
	/**
	 * Batch cache key for the JOB Execution Parameters.
	 */
	public static final String BATCH_JOB_EXECUTION_PARAMS = "BATCH_JOB_EXECUTION_PARAMS";
	/**
	 * Batch Cache key used for Indexing JobInstanceID and Parameter.
	 * 
	 */
	public static final String BATCH_INSTANCE_ID_PARAMS = "BATCH_INSTANCE_ID_PARAMS";
	/**
	 * Batch cache key for the JOB Execution Parameters.
	 */
	public static final String BATCH_JOB_INSTANCE = "BATCH_JOB_INSTANCE";
	/**
	 * Cache Key to link JOB_ID to STEP_ID. The id referred here are executions
	 * id and not the instance id.<br>
	 * There is a one to many relationship for Job to step id as shown below
	 * JOB_ID -> STEP_ID* This relationship services as an index for fast
	 * access.
	 */
	public static final String JOB_ID_STEP_ID_LIST_PREFIX = "JOB_ID_STEP_ID_LIST_";
	/**
	 * Cache Key to link JOB_INSTANCE_ID to JOB_EXECUTION_ID..<br>
	 * There is a one to many relationship for JOB_INSTANCE_ID to
	 * JOB_EXECUTION_ID or simply expressed as <br>
	 * JOB_INSTANCE_ID -> JOB_EXECUTION_ID* This relationship services as an
	 * index for fast access.
	 */
	public static final String JOB_INSTANCE_EXEC_ID_LIST_PREFIX = "JOB_INSTANCE_EXEC_ID_LIST_";
	/**
	 * Cache Key to link JOB_EXECUTION_ID to JOB_INSTANCE_ID ..<br>
	 * There is a one to one relationship for JOB_INSTANCE_ID to
	 * JOB_EXECUTION_ID or simply expressed as <br>
	 * JOB_EXECUTION_ID -> JOB_INSTANCE_ID This relationship services as an
	 * index for fast access.
	 */
	public static final String JOB_EXEC_INSTANCE_ID_PREFIX = "JOB_EXEC_INSTANCE_ID_";
	/**
	 * Cache Key to link JOB_NAME to JOB_INSTANCE_ID <br>
	 * There is a one to many relationship for Job to step id as shown below
	 * JOB_NAME -> JOB_INSTANCE_ID* This relationship services as an index for
	 * fast access.
	 */
	public static final String JOB_NAME_ISTANCE_ID_LIST_PREFIX = "JOB_NAME_ISTANCE_ID_LIST_";
	/**
	 * Batch cache key for the Running sequence for the JOB_EXECUTION_ID.
	 */
	public static final String BATCH_JOB_EXECUTION_SEQ = "BATCH_JOB_EXECUTION_SEQ";
	/**
	 * Batch cache key for the Running sequence for the STEP_EXECUTION_ID.
	 */
	public static final String BATCH_STEP_EXECUTION_SEQ = "BATCH_STEP_EXECUTION_SEQ";
	/**
	 * Batch cache key for the Running sequence for the JOB_SEQ.
	 */
	public static final String BATCH_JOB_SEQ = "BATCH_JOB_SEQ";
	/**
	 * Batch cache key for the Running sequence for the JOB Archivals.
	 */
	public static final String BATCH_ARCHIVAL_SEQ = "BATCH_ARCHIVAL_SEQ";

	/**
	 * A key for all batches started by current runtime instance.
	 */
	public static final String NODE_BATCH_STARTED_EXECUTIONS = "NODE_BATCH_EXECUTIONS";
	/**
	 * A key for all batches started by current runtime instance.
	 */
	public static final String NODE_BATCH_COMPLETED_EXECUTIONS = "NODE_BATCH_EXECUTIONS";

	/**
	 * A key for execution that met with warning for File system space
	 */
	
	/**
	 * jobKey Generator
	 */

	private JobKeyGenerator<JobParameters> jobKeyGenerator = new DefaultJobKeyGenerator();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.ExecutionContextDao#
	 * getExecutionContext(org.springframework.batch.core.JobExecution)
	 */

	@Override
	public ExecutionContext getExecutionContext(final JobExecution jobExecution) {
		Long executionId = null;
		ExecutionContext context;
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getExecutionContext() started ");
		}

		try {
			executionId = jobExecution.getId();
			Assert.notNull(executionId, "ExecutionId must not be null.");
			context = (ExecutionContext) getCache().getFromMap(
					BATCH_JOB_EXECUTION_CONTEXT, executionId);
			if (context == null) {
				context = new ExecutionContext();
			}
			return context;
		} finally {
			executionId = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("TestStore.getExecutionContext() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.ExecutionContextDao#
	 * getExecutionContext(org.springframework.batch.core.StepExecution)
	 */
	@Override
	public ExecutionContext getExecutionContext(
			final StepExecution stepExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getExecutionContext() started ");
		}
		Long executionId = null;
		ExecutionContext context;
		try {
			Assert.notNull(stepExecution, "Step Execution cannot be null");
			executionId = stepExecution.getId();
			Assert.notNull(executionId, "ExecutionId must not be null.");
			context = (ExecutionContext) getCache().getFromMap(
					BATCH_STEP_EXECUTION_CONTEXT, executionId);
			if (context == null) {
				context = new ExecutionContext();
			}
			return context;
		} finally {
			executionId = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("TestStore.getExecutionContext() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.ExecutionContextDao#
	 * saveExecutionContext(org.springframework.batch.core.JobExecution)
	 */
	@Override
	public void saveExecutionContext(final JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.saveExecutionContext() started ");
		}
		try {
			Assert.notNull(jobExecution, "jobExecution must not be null.");
			Assert.notNull(jobExecution.getExecutionContext(),
					"jobExecutionContext must not be null.");
			Assert.notNull(jobExecution.getId(), "ExecutionId must not be null");

			getCache().putToMap(BATCH_JOB_EXECUTION_CONTEXT,
					jobExecution.getId(), jobExecution.getExecutionContext());
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.saveExecutionContext() completed");
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.ExecutionContextDao#
	 * saveExecutionContext(org.springframework.batch.core.StepExecution)
	 */
	@Override
	public void saveExecutionContext(final StepExecution stepExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.saveExecutionContext() started ");
		}

		try {
			Assert.notNull(stepExecution, "stepExecution must not be null.");
			Assert.notNull(stepExecution.getExecutionContext(),
					"stepExecutionContext must not be null.");
			Assert.notNull(stepExecution.getId(),
					"ExecutionId must not be null");
			if (stepExecution.getExecutionContext() != null) {
				getCache().putToMap(BATCH_STEP_EXECUTION_CONTEXT,
						stepExecution.getId(),
						stepExecution.getExecutionContext());

			}
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.saveExecutionContext() completed");
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.ExecutionContextDao#
	 * saveExecutionContexts(java.util.Collection)
	 */
	@Override
	public void saveExecutionContexts(
			final Collection<StepExecution> stepExecutions) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.saveExecutionContexts() started ");
		}
		Assert.notNull(stepExecutions,
				"the colleciton stepExecutions cannot be null");

		try {
			for (StepExecution stepExecution : stepExecutions) {
				saveExecutionContext(stepExecution);
				saveExecutionContext(stepExecution.getJobExecution());
			}

		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.saveExecutionContexts() completed");
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.ExecutionContextDao#
	 * updateExecutionContext(org.springframework.batch.core.JobExecution)
	 */
	@Override
	public void updateExecutionContext(final JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.updateExecutionContext() started ");
		}

		Long executionId = jobExecution.getId();
		ExecutionContext executionContext = jobExecution.getExecutionContext();
		Assert.notNull(executionId, "ExecutionId must not be null.");
		Assert.notNull(executionContext,
				"The ExecutionContext must not be null.");

		try {
			if (getCache()
					.hasKeyInMap(BATCH_JOB_EXECUTION_CONTEXT, executionId)) {
				saveExecutionContext(jobExecution);
			}

		} finally {
			executionContext = null;
			executionId = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.updateExecutionContext() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.ExecutionContextDao#
	 * updateExecutionContext(org.springframework.batch.core.StepExecution)
	 */
	@Override
	public void updateExecutionContext(final StepExecution stepExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.updateExecutionContext() started ");
		}
		Long executionId = stepExecution.getId();
		Assert.notNull(executionId, "ExecutionId must not be null.");
		try {
			saveExecutionContext(stepExecution);
		} finally {
			executionId = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.updateExecutionContext() completed");
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.StepExecutionDao#
	 * addStepExecutions(org.springframework.batch.core.JobExecution)
	 */
	@Override
	public void addStepExecutions(final JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.addStepExecutions() started ");
		}
		Assert.notNull(jobExecution, "The job Execution element cannot be null");
		Assert.notNull(jobExecution.getId(),
				"The jobExecutionID element cannot be null");

		List<Long> stepExecutionIds = null;
		List<StepExecution> stepExecutions = null;

		try {

			stepExecutionIds = getStepExecutionIds(jobExecution.getId());
			if (CollectionUtils.isNotEmpty(stepExecutionIds)) {
				Collections.sort(stepExecutionIds);
				stepExecutions = new ArrayList<StepExecution>(
						stepExecutionIds.size());
				for (Long stepExecutionId : stepExecutionIds) {
					stepExecutions.add(getStepExecution(stepExecutionId));
				}
				if (CollectionUtils.isNotEmpty(stepExecutions)) {
					jobExecution.addStepExecutions(stepExecutions);
				}
			}

		} finally {
			stepExecutionIds = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.addStepExecutions() completed");
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.StepExecutionDao#
	 * getStepExecution(org.springframework.batch.core.JobExecution,
	 * java.lang.Long)
	 */
	@Override
	public StepExecution getStepExecution(final JobExecution jobExecution,
			final Long stepExecutionId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getStepExecution() started ");
		}

		List<Long> stepExecutionIds = null;
		try {
			Assert.notNull(jobExecution, "Job Execution cannot be null");
			Assert.notNull(jobExecution.getId(),
					"JobExecutionId cannot be null");
			Assert.notNull(stepExecutionId, "StepExecutionId cannot be null");
			stepExecutionIds = getStepExecutionIds(jobExecution.getId());
			if (CollectionUtils.isNotEmpty(stepExecutionIds)
					&& stepExecutionIds.contains(stepExecutionId)) {
				return getStepExecution(stepExecutionId);
			} else {
				return null;
			}

		} finally {
			stepExecutionIds = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getStepExecution() completed");
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.StepExecutionDao#
	 * saveStepExecution(org.springframework.batch.core.StepExecution)
	 */
	@Override
	public void saveStepExecution(final StepExecution stepExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.saveStepExecution() started ");
		}

		try {
			Assert.isNull(stepExecution.getId(),
					"to-be-saved (not updated) StepExecution can't already have an id assigned");
			Assert.isNull(stepExecution.getVersion(),
					"to-be-saved (not updated) StepExecution can't already have a version assigned");
			validateStepExecution(stepExecution);
			stepExecution.setId(nextStepExecutionId());
			stepExecution.incrementVersion();

			put(stepExecution);
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.saveStepExecution() completed");
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.StepExecutionDao#
	 * saveStepExecutions(java.util.Collection)
	 */
	@Override
	public void saveStepExecutions(Collection<StepExecution> stepExecutions) {
		if (CollectionUtils.isNotEmpty(stepExecutions)) {
			for (StepExecution stepExecution : stepExecutions) {
				saveStepExecution(stepExecution);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.StepExecutionDao#
	 * updateStepExecution(org.springframework.batch.core.StepExecution)
	 */
	@Override
	public void updateStepExecution(final StepExecution stepExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.updateStepExecution() started ");
		}
		StepExecution stepExecutionCopy = null;
		try {
			validateStepExecution(stepExecution);
			Assert.notNull(
					stepExecution.getId(),
					"StepExecution Id cannot be null. StepExecution must saved before it can be updated.");
			stepExecutionCopy = getStepExecution(stepExecution.getId());
			if (stepExecutionCopy != null
					&& stepExecutionCopy.getVersion().intValue() == stepExecution
							.getVersion().intValue()) {
				stepExecution.incrementVersion();
				put(stepExecution);
				



			} else {
				throw new OptimisticLockingFailureException(
						"Attempt to update job execution id="
								+ stepExecution.getId()
								+ " failed with  version ("
								+ stepExecution.getVersion()
								+ "), "
								+ (stepExecutionCopy != null ? "where current version is "
										+ stepExecutionCopy.getVersion()
										: " as no such stepExecution exists"));
			}
		} finally {
			stepExecutionCopy = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.updateStepExecution() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.JobExecutionDao#
	 * findJobExecutions(org.springframework.batch.core.JobInstance)
	 */
	@Override
	public List<JobExecution> findJobExecutions(final JobInstance jobInstance) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.findJobExecutions() started ");
		}
		List<Long> jobExecutionIds = null;
		List<JobExecution> jobExecutions = null;
		try {
			jobExecutionIds = getJobExecutionsIds(jobInstance.getId());
			if (CollectionUtils.isNotEmpty(jobExecutionIds)) {
				jobExecutions = new ArrayList<JobExecution>(
						jobExecutionIds.size());
				Collections.sort(jobExecutionIds);
				for (int i = jobExecutionIds.size() - 1; i >= 0; i--) {
					jobExecutions.add(getJobExecution(jobExecutionIds.get(i)));
				}
			}
			return jobExecutions;
		} finally {
			jobExecutionIds = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.findJobExecutions() completed");
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.JobExecutionDao#
	 * findRunningJobExecutions(java.lang.String)
	 */
	@Override
	public Set<JobExecution> findRunningJobExecutions(String jobName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.findRunningJobExecutions() started ");
		}
		List<Long> jobInstanceIds = null;
		List<Long> jobExecutionIds = null;
		Set<JobExecution> jobExecutions = null;
		JobExecution execution = null;
		try {
			jobInstanceIds = getJobInstanceIds(jobName);
			if (CollectionUtils.isNotEmpty(jobInstanceIds)) {
				jobExecutions = new HashSet<JobExecution>();
				for (Long jobInstanceId : jobInstanceIds) {
					jobExecutionIds = getJobExecutionsIds(jobInstanceId);
					if (CollectionUtils.isNotEmpty(jobExecutionIds)) {
						for (Long jobExecutionId : jobExecutionIds) {
							execution = getJobExecution(jobExecutionId);
							if (execution != null
									&& execution.getEndTime() != null) {
								jobExecutions.add(execution);
							}
							execution = null;
						}
						jobExecutionIds = null;
					}
				}
			}
			return jobExecutions;
		} finally {
			jobInstanceIds = null;
			jobExecutionIds = null;
			execution = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.findRunningJobExecutions() completed");
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.JobExecutionDao#
	 * getLastJobExecution(org.springframework.batch.core.JobInstance)
	 */
	@Override
	public JobExecution getLastJobExecution(JobInstance jobInstance) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getLastJobExecution() started ");
		}
		JobExecution jobExecution = null;
		List<Long> jobExecutionIds;
		try {

			jobExecutionIds = getJobExecutionsIds(jobInstance.getId());
			if (CollectionUtils.isNotEmpty(jobExecutionIds)) {
				Collections.sort(jobExecutionIds);
				jobExecution = getJobExecution(jobExecutionIds
						.get(jobExecutionIds.size() - 1));
			}
			return jobExecution;
		} finally {
			jobExecutionIds = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getLastJobExecution() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.JobExecutionDao#
	 * saveJobExecution(org.springframework.batch.core.JobExecution)
	 */
	@Override
	public void saveJobExecution(JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.saveJobExecution() started ");
		}

		try {
			validateJobExecution(jobExecution);
			jobExecution.incrementVersion();
			jobExecution.setId(nextJobExecutionId());
			put(jobExecution);

		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.saveJobExecution() completed");
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.JobExecutionDao#
	 * synchronizeStatus(org.springframework.batch.core.JobExecution)
	 */
	@Override
	public void synchronizeStatus(JobExecution jobExceution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.synchronizeStatus() started ");
		}
		JobExecution lastJobExecution;
		try {
			Assert.notNull(jobExceution, "The JobExecution cannot be null");
			Assert.notNull(jobExceution.getId(),
					"The JobExecutionId cannot be null");
			Assert.notNull(jobExceution.getVersion(),
					"The JobExecution version cannot be null");
			lastJobExecution = getJobExecution(jobExceution.getId());
			Assert.notNull(lastJobExecution);
			if (lastJobExecution != null
					&& lastJobExecution.getVersion() != jobExceution
							.getVersion().intValue()) {
				jobExceution.setStatus(lastJobExecution.getStatus());
				jobExceution.setVersion(lastJobExecution.getVersion());
			}
		} finally {
			lastJobExecution = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.synchronizeStatus() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.JobExecutionDao#
	 * updateJobExecution(org.springframework.batch.core.JobExecution)
	 */
	@Override
	public void updateJobExecution(final JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.updateJobExecution() started ");
		}
		JobExecution copy = null;
		try {
			validateJobExecution(jobExecution);
			Assert.notNull(
					jobExecution.getId(),
					"JobExecution ID cannot be null. JobExecution must be saved before it can be updated");
			Assert.notNull(
					jobExecution.getVersion(),
					"JobExecution version cannot be null. JobExecution must be saved before it can be updated");
			if (getCache().hasKeyInMap(BATCH_JOB_EXECUTION,
					jobExecution.getId())) {
				copy = getJobExecution(jobExecution.getId());
				if (copy != null
						&& copy.getVersion().intValue() == jobExecution
								.getVersion().intValue()) {
					jobExecution.incrementVersion();
				// setting the Filesystem threshold warning in exit description	
					setExitDescription(jobExecution);
					
					put(jobExecution);

				} else {
					throw new OptimisticLockingFailureException(
							"Attempt to update job execution id="
									+ jobExecution.getId()
									+ " failed with  version ("
									+ jobExecution.getVersion()
									+ (copy != null ? "), where current version is "
											+ copy.getVersion()
											: "where as no such JobExecution exists"));
				}
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.updateJobExecution() completed");
			}
		}
	}

	

	public void setExitDescription(JobExecution jobExecution){
		
		BatchStatus batchStatus =  jobExecution.getStatus().getBatchStatus();
		if(batchStatus!= null ){
			String currentDescription = null;
			String newDescription = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.setExitDescription() starts");
			}
			if(BatchStatus.FAILED.equals(batchStatus) || BatchStatus.STOPPED.equals(batchStatus) || BatchStatus.COMPLETED.equals(batchStatus)){
				
				newDescription =  getWarningForExecutionId(jobExecution.getId());
				if(newDescription != null && newDescription !=""){
					 currentDescription = jobExecution.getExitStatus().getExitDescription();
				 if(!currentDescription.contentEquals("Warnings during Execution :")){
					 currentDescription =  currentDescription + "\nWarnings during Execution :  \n";
				  }
				     newDescription  = currentDescription +newDescription;
				  jobExecution.setExitStatus(jobExecution.getExitStatus().addExitDescription( newDescription));
				  removeWarningForExecutionId(jobExecution.getId());
				  
				 if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("SmartBatchRedisRuntimeStore.setExitDescription()currentDescription completed");
					}
				}
			}		
		}
		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.JobInstanceDao#
	 * createJobInstance(java.lang.String,
	 * org.springframework.batch.core.JobParameters)
	 */
	@Override
	public JobInstance createJobInstance(final String jobName,
			final JobParameters jobParameters) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.createJobInstance() started ");
		}
		Assert.notNull(jobParameters, "JobParameters must not be null.");
		Assert.state(getJobInstance(jobName, jobParameters) == null,
				"JobInstance must not already exist");
		try {
			JobInstance jobInstance = new JobInstance(nextJobInstanceId(),
					jobName);
			jobInstance.incrementVersion();
			put(jobInstance);
			linkJobParametersInstanceID(jobParameters, jobName,
					jobInstance.getId());
			return jobInstance;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.createJobInstance() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.repository.dao.JobInstanceDao#getJobInstance
	 * (java.lang.Long)
	 */
	@Override
	public JobInstance getJobInstance(final Long jobInstanceId) {
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobParamters() started ");
			}
			Assert.notNull(jobInstanceId, "Job Instance Id cannot be null");
			return (JobInstance) getCache().getFromMap(BATCH_JOB_INSTANCE,
					jobInstanceId);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobParamters() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.repository.dao.JobInstanceDao#getJobInstance
	 * (org.springframework.batch.core.JobExecution)
	 */
	@Override
	public JobInstance getJobInstance(JobExecution jobExeution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstance() started ");
		}
		Long jobInstanceId = null;
		JobInstance jobInstance = null;
		try {
			Assert.notNull(jobExeution, "JobExecution Cannot be null");
			Assert.notNull(jobExeution.getId(), "JobExecutionID Cannot be null");
			jobInstanceId = getJobInstanceId(jobExeution.getId());
			if (jobInstanceId != null) {
				jobInstance = getJobInstance(jobInstanceId);
			}
			return jobInstance;
		} finally {
			jobInstanceId = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstance() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.repository.dao.JobInstanceDao#getJobInstance
	 * (java.lang.String, org.springframework.batch.core.JobParameters)
	 */
	@Override
	public JobInstance getJobInstance(String jobName,
			JobParameters jobParameters) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstance() started ");
		}
		Assert.notNull(jobName, "Job name cannot be null");
		Assert.notNull(jobParameters, "JobParameters name cannot be null");
		Long jobInstanceId = null;
		JobInstance instance = null;
		try {
			jobInstanceId = getJobInstanceId(jobParameters, jobName);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The job instance id is..." + jobInstanceId);
			}
			if (jobInstanceId != null) {
				instance = getJobInstance(jobInstanceId);
			}
			return instance;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstance() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.JobInstanceDao#
	 * getJobInstanceCount(java.lang.String)
	 */
	@Override
	public int getJobInstanceCount(String jobName) throws NoSuchJobException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstanceCount() started ");
		}
		List<Long> jobInstanceIds;
		Assert.notNull(jobName, "Job name cannot be null");
		try {
			jobInstanceIds = getJobInstanceIds(jobName);
			if (CollectionUtils.isNotEmpty(jobInstanceIds)) {
				return jobInstanceIds.size();
			} else {
				throw new NoSuchJobException(
						"No job instances were found for job name " + jobName);
			}
		} finally {
			jobInstanceIds = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstanceCount() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.repository.dao.JobInstanceDao#getJobInstances
	 * (java.lang.String, int, int)
	 */
	@Override
	public List<JobInstance> getJobInstances(final String jobName,
			final int start, final int count) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstances() started ");
		}
		List<Long> jobInstanceIds;
		List<JobInstance> jobInstances = new ArrayList<JobInstance>();
		Assert.notNull(jobName, "Job Name cannot be null");
		try {
			jobInstanceIds = getJobInstanceIds(jobName);
			if (CollectionUtils.isNotEmpty(jobInstanceIds)) {
				Collections.sort(jobInstanceIds);
				int currentCount = 0;
				for (int i = jobInstanceIds.size() - (1 + start); i >= 0
						&& (++currentCount <= count); i--) {
					if (i < 0) {
						break;
					}
					jobInstances.add(getJobInstance(jobInstanceIds.get(i)));
				}
			}
			return jobInstances;
		} finally {
			jobInstanceIds = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstances() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.repository.dao.JobInstanceDao#getJobNames
	 * ()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getJobNames() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getJobNames() started ");
		}
		Set<String> jobNames = new HashSet<String>();
		HashSet<Object> keys = null;
		JobInstance instance = null;
		List<String> jobNamesSorted = new ArrayList<String>();
		try {
			keys = (HashSet<Object>) getCache().getAllKeys(BATCH_JOB_INSTANCE);
			if (CollectionUtils.isNotEmpty(keys)) {
				for (Object key : keys) {
					if (key != null) {
						instance = getJobInstance((Long) key);
						jobNames.add(instance.getJobName());
					}
					instance = null;
					key = null;
				}
			}
			if (CollectionUtils.isNotEmpty(jobNames)) {
				jobNamesSorted.addAll(jobNames);
				Collections.sort(jobNamesSorted);
			}
			return jobNamesSorted;
		} finally {
			keys = null;
			jobNames = null;
			instance = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobNames() completed");
			}
		}
	}

	private void validateJobExecution(JobExecution jobExecution) {
		Assert.notNull(jobExecution);
		Assert.notNull(jobExecution.getJobId(),
				"JobExecution Job-Id cannot be null.");
		Assert.notNull(jobExecution.getStatus(),
				"JobExecution status cannot be null.");
		Assert.notNull(jobExecution.getCreateTime(),
				"JobExecution create time cannot be null");
	}

	private void validateStepExecution(StepExecution stepExecution) {
		Assert.notNull(stepExecution);
		Assert.notNull(stepExecution.getStepName(),
				"StepExecution step name cannot be null.");
		Assert.notNull(stepExecution.getStartTime(),
				"StepExecution start time cannot be null.");
		Assert.notNull(stepExecution.getStatus(),
				"StepExecution status cannot be null.");
	}

	/**
	 * Provides the list of JobExecutionId per JobInstanceId.
	 * 
	 * @param jobInstanceId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getJobExecutionsIds(Long jobInstanceId) {
		String key = concat(JOB_INSTANCE_EXEC_ID_LIST_PREFIX, jobInstanceId);
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobExecutionsIds() started ");
			}
			return (List<Long>) getCache().getList(key);
		} finally {
			key = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobExecutionsIds() completed");
			}
		}
	}

	/**
	 * Method to link a JobExecutionId to a JobInstanceId The implementation
	 * should keep in mind that this should be linked only during the creation
	 * of jobExecutionId to avoid duplicate linking.
	 * 
	 * @param jobInstanceId
	 * @param jobExecutionId
	 */
	public void linkJobInstanceExecution(Long jobInstanceId, Long jobExecutionId) {
		String key = concat(JOB_INSTANCE_EXEC_ID_LIST_PREFIX, jobInstanceId);
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.linkJobExecutionInstance() started ");
			}
			getCache().appendTolist(key, jobExecutionId);
		} finally {
			key = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.linkJobExecutionInstance() completed");
			}
		}

	}

	/**
	 * Method to link a JobExecutionId to a JobInstanceId The implementation
	 * should keep in mind that this should be linked only during the creation
	 * of jobExecutionId to avoid duplicate linking.
	 * 
	 * @param jobInstanceId
	 * @param jobExecutionId
	 */
	public void linkJobExecutionInstance(Long jobExecutionId, Long jobInstanceId) {
		String key = concat(JOB_EXEC_INSTANCE_ID_PREFIX, jobExecutionId);
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.linkJobExecutionInstance() started ");
			}
			getCache().add(key, jobInstanceId);
		} finally {
			key = null;

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.linkJobExecutionInstance() completed");
			}
		}
	}

	/**
	 * Provides the JobExecutionId based on JobInstanceId
	 * 
	 * @param jobInstanceId
	 * @return
	 */
	public Long getJobInstanceId(Long jobExecutionId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstanceId() started ");
		}
		try {
			return (Long) getCache().get(
					concat(JOB_EXEC_INSTANCE_ID_PREFIX, jobExecutionId));
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstanceId() completed");
			}
		}
	}

	/**
	 * Method to link a JobParametersKey to a jobExecutionId The implementation
	 * should keep in mind that this should be linked only during the creation
	 * of jobExecutionId to avoid duplicate linking.
	 * 
	 * @param jobInstanceId
	 * @param jobExecutionId
	 */
	public void linkJobParametersInstanceID( final JobParameters jobParameters,
			 final String jobName, final Long jobInstanceId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.linkJobParametersInstanceID() started ");
		}
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Creating the job parameters key for parameters :"+jobParameters+ " \n and the key is "+concat(jobName,
						jobKeyGenerator.generateKey(jobParameters)));
			}
			getCache()
					.putToMap(
							BATCH_INSTANCE_ID_PARAMS,
							concat(jobName,
									jobKeyGenerator.generateKey(jobParameters)),
							jobInstanceId);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.linkJobParametersInstanceID() completed");
			}
		}
	}

	/**
	 * Provides the JobInstanceId based on JobParametersKey.
	 * 
	 * @param jobParameters
	 * @param jobName
	 * @return
	 */
	public Long getJobInstanceId(JobParameters jobParameters, String jobName) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Fetching job instance id for parameters :"+jobParameters+ " \n and the key is "+concat(jobName,
					jobKeyGenerator.generateKey(jobParameters)));
		}
		return (Long) getCache().getFromMap(BATCH_INSTANCE_ID_PARAMS,
				concat(jobName, jobKeyGenerator.generateKey(jobParameters)));
	}

	/**
	 * 
	 * Method to link a JobExecutionId to a JobInstanceId The implementation
	 * should keep in mind that this should be linked only during the creation
	 * of jobExecutionId to avoid duplicate linking.
	 * 
	 * @param jobName
	 * @param jobInstanceId
	 */
	public void linkJobInstanceName(String jobName, Long jobInstanceId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.linkJobInstanceName() started ");
		}
		String key = concat(JOB_NAME_ISTANCE_ID_LIST_PREFIX, jobName);
		try {
			getCache().appendTolist(key, jobInstanceId);
		} finally {
			key = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.linkJobInstanceName() completed");
			}
		}
	}

	/**
	 * Provides the list of JobInstanceId per JobName.
	 * 
	 * @param jobName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getJobInstanceIds(String jobName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstanceIds() started ");
		}
		String key = concat(JOB_NAME_ISTANCE_ID_LIST_PREFIX, jobName);
		try {

			return (ArrayList<Long>) getCache().getList(key);
		} finally {
			key = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobInstanceIds() completed");
			}
		}
	}

	/**
	 * Provides the list of StepExecutionIds per JobExecutionId
	 * 
	 * @param jobExecutionId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getStepExecutionIds(final Long jobExecutionId) {
		String key = concat(JOB_ID_STEP_ID_LIST_PREFIX, jobExecutionId);
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getStepExecutionIds() started ");
			}
			return (ArrayList<Long>) getCache().getList(key);
		} finally {
			key = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getStepExecutionIds() completed");
			}
		}

	}

	/**
	 * Method to link a StepExecutionId to a JobExecutionId The implementation
	 * should keep in mind that this should be linked only during the creation
	 * of stepExeution to avoid duplicate linking
	 * 
	 * @param jobExecutionId
	 * @param stepExecutionId
	 */
	public void linkStepJobIds(Long stepExecutionId, Long jobExecutionId) {
		String key = concat(JOB_ID_STEP_ID_LIST_PREFIX, jobExecutionId);
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.linkStepJobIds() started ");
			}
			getCache().appendTolist(key, stepExecutionId);
		} finally {
			key = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.linkStepJobIds() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchStore#linkNodeStartedJobs(java.lang.Long)
	 */
	@Override
	public void linkNodeStartedJobs(Long jobExecutionId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.linkNodeStartedJobs() started");
		}
		try {

			getCache().appendTolistNoNs(
					concat(NODE_BATCH_STARTED_EXECUTIONS, NODE_NAME),
					concat(ThreadContextUtils.getNamespace(), HYPHEN_CHAR,
							jobExecutionId));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.linkNodeStartedJobs() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchStore#deLinkNodeStartedJobs(java.lang.Long)
	 */
	@Override
	public void deLinkNodeStartedJobs(Long jobExecutionId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.deLinkNodeStartedJobs() started");
		}
		try {
			getCache().removeFromListNoNs(
					concat(NODE_BATCH_STARTED_EXECUTIONS, NODE_NAME),
					-1,
					concat(ThreadContextUtils.getNamespace(), HYPHEN_CHAR,
							jobExecutionId));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.deLinkNodeStartedJobs() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchStore#getNodeStartedJobs()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getNodeStartedJobIds() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getNodeStartedJobs() started");
		}
		try {
			return (List<String>) getCache().getListNoNs(
					concat(NODE_BATCH_STARTED_EXECUTIONS, NODE_NAME));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getNodeStartedJobs() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchStore#getNodeCompletedJobs()
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getNodeCompletedJobIds() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getNodeCompletedJobs() started");
		}
		try {
			return (List<Long>) getCache().getListNoNs(
					concat(NODE_BATCH_COMPLETED_EXECUTIONS, NODE_NAME));

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getNodeCompletedJobs() completed");
			}
		}
	}

	/**
	 * Get the {@link StepExecution} from the store based on stepExecutionId.
	 * 
	 * @param stepExecutionId
	 * @return {@link StepExecution}
	 */
	public StepExecution getStepExecution(Long stepExecutionId) {
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getStepExecution() started ");
			}
			return (StepExecution) getCache().getFromMap(BATCH_STEP_EXECUTION,
					stepExecutionId);
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getStepExecution() completed");
			}
		}

	}

	/**
	 * 
	 */
	@Override
	public JobExecution getJobExecution(Long jobExecutionId) {
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobExecution() started ");
			}
			return (JobExecution) getCache().getFromMap(BATCH_JOB_EXECUTION,
					jobExecutionId);
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobExecution() completed");
			}

		}
	}

	/**
	 * Get the {@link JobParameters} from the store based on the jobExecutionId.
	 * 
	 * @param jobExecutionId
	 * @return
	 */
	public JobParameters getJobParamters(Long jobExecutionId) {
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobParamters() started ");
			}
			return (JobParameters) getCache().getFromMap(
					BATCH_JOB_EXECUTION_PARAMS, jobExecutionId);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getJobParamters() completed");
			}
		}
	}

	/**
	 * Upsert (Insert/Update) the {@link JobExecution}.
	 * 
	 * @param jobExecution
	 */
	public void put(final JobExecution jobExecution) {
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.upsertJobExecution() started ");
			}
			boolean isInsert = !getCache().hasKeyInMap(BATCH_JOB_EXECUTION,
					jobExecution.getId());
			getCache().putToMap(BATCH_JOB_EXECUTION, jobExecution.getId(),
					jobExecution);
			if (isInsert) {
				linkJobInstanceExecution(jobExecution.getJobInstance().getId(),
						jobExecution.getId());
				linkJobExecutionInstance(jobExecution.getId(), jobExecution
						.getJobInstance().getId());
				put(jobExecution.getId(), jobExecution.getJobParameters());
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.upsertJobExecution() completed");
			}
		}
	}

	/**
	 * Upsert (Insert/Update) the {@link StepExecution}.
	 * 
	 * @param stepExecution
	 */
	public void put(final StepExecution stepExecution) {
		ExecutionContext executionContext = ThreadContextUtils
				.getExecutionContext();
		ExecutionContext stepExecutionContext = null;
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.upsertStepExecution() started ");
			}

			boolean isInsert = !getCache().hasKeyInMap(BATCH_STEP_EXECUTION,
					stepExecution.getId());
			if (executionContext != null) {
				if (stepExecution.getExecutionContext() == null) {
					stepExecution.setExecutionContext(executionContext);

				} else {
					stepExecutionContext = stepExecution.getExecutionContext();
					for (Map.Entry<String, Object> entry : executionContext
							.entrySet()) {
						stepExecutionContext.put(entry.getKey(),
								entry.getValue());
						entry = null;
					}
					stepExecution.setExecutionContext(stepExecutionContext);
				}
				saveExecutionContext(stepExecution);
				if (LOGGER.isDebugEnabled()) {
				    LOGGER.debug("Step Status : Started : " + BatchStatus.STARTED.equals(stepExecution.getStatus()) + 
					    	 " Starting : " +
						BatchStatus.STARTING.equals(stepExecution.getStatus()));
				}
				if (!("STARTED".equalsIgnoreCase(stepExecution.getStatus().toString())
					|| "STARTING".equalsIgnoreCase(stepExecution.getStatus().toString()))) {
				if (LOGGER.isDebugEnabled()) {
				    LOGGER.debug("Clearing the thread Execution context for Step Status : " + stepExecution.getStatus().toString());
				}
				ThreadContextUtils.clearExecutionContext();
				}
			}
			getCache().putToMap(BATCH_STEP_EXECUTION, stepExecution.getId(),
					stepExecution);
			if (isInsert) {
				linkStepJobIds(stepExecution.getId(),
						stepExecution.getJobExecutionId());
			}
		} finally {
			executionContext = null;
			stepExecutionContext = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.upsertStepExecution() completed");
			}

		}
	}

	/**
	 * Upsert (Insert/Update) the {@link JobInstance}.
	 * 
	 * @param jobInstance
	 */
	public void put(JobInstance jobInstance) {
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.upsertJobInstance() started ");
			}
			boolean isInsert = !getCache().hasKeyInMap(BATCH_JOB_INSTANCE,
					jobInstance.getId());

			getCache().putToMap(BATCH_JOB_INSTANCE, jobInstance.getId(),
					jobInstance);
			if (isInsert) {
				linkJobInstanceName(jobInstance.getJobName(),
						jobInstance.getId());
			}
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.upsertJobInstance() completed");
			}
		}
	}

	/**
	 * Upsert (Insert/Update) the {@link jobParameters} .
	 * 
	 * @param stepExecution
	 */
	public void put(final Long jobExecutionid, final JobParameters jobParameters) {
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.upsertJobParameters() started ");
			}
			getCache().putToMap(BATCH_JOB_EXECUTION_PARAMS, jobExecutionid,
					jobParameters);

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.upsertJobParameters() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchStore#incrementAndGet(java.lang.String)
	 */
	public Long incrementAndGet(final String sequenceID) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.incrementAndGet() started ");
		}
		Assert.notNull(sequenceID, "sequence Id is cannot be null");
		try {
			return getCache().incrementAndGet(sequenceID);
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.incrementAndGet() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchStore#nextJobExecutionId()
	 */
	public Long nextJobExecutionId() {
		return incrementAndGet(BATCH_JOB_EXECUTION_SEQ);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchStore#nextJobInstanceId()
	 */
	public Long nextJobInstanceId() {
		return incrementAndGet(BATCH_JOB_SEQ);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchStore#nextStepExecutionId()
	 */
	public Long nextStepExecutionId() {
		return incrementAndGet(BATCH_STEP_EXECUTION_SEQ);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchRuntimeStore#archive(java.util.ArrayList,
	 * java.lang.Long, java.lang.Long, java.lang.String, java.lang.String)
	 */
	@Override
	public Long archive(ArrayList<String> batchNames, Long startEpoch,
			Long endEpoch, String description, String type)
			throws BatchException {
		// TODO Auto-generated method stub

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.archiveCompleted() started");
		}
		Long archivalSeq = -1L;
		Archiver archiver = null;
		try {
			archivalSeq = incrementAndGet(BATCH_ARCHIVAL_SEQ);
			archiver = new RedisArchiver(this, batchMetaStore, batchNames,
					startEpoch, endEpoch, archivalSeq, Archiver.ARCHIVE,
					description, type);
			SmartBatchExecutor.getExecutor().submit(archiver);
			return archivalSeq;
		} finally {
			archiver = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.archiveCompleted() completed");
			}
		}

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchRuntimeStore#getArchivalDetails(java.lang
	 * .Long)
	 */
	@Override
	public ArchivalInstanceDetails getArchivalDetails(Long id) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getArchivalDetails() started");
		}
		try {
			return getCache().getFromMap(ARCHIVER_DETAILS, id,
					ArchivalInstanceDetails.class);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getArchivalDetails() completed");
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Long> getArchivalInstances() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getArchivalInstances() started");
		}
		try {
			return (Set<Long>) getCache().getAllKeys(ARCHIVER_DETAILS);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getArchivalInstances() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchRuntimeStore#loadArchive(java.lang.String)
	 */
	@Override
	public Long restoreFromArchive(String dumpFile) throws BatchException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.restoreFromArchive() started");
		}
		Long archivalSeq = -1L;
		Archiver archiver = null;
		try {
			archivalSeq = incrementAndGet(BATCH_ARCHIVAL_SEQ);
			archiver = new RedisArchiver(this, batchMetaStore, archivalSeq,
					dumpFile, Archiver.RESTORE);
			SmartBatchExecutor.getExecutor().submit(archiver);
			return archivalSeq;
		} finally {
			archiver = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.restoreFromArchive() completed");
			}
		}
	}


	public static final SmartBatchCache getCache() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getCache() started");
		}

		try {
			return CACHE_FACTORY.getCache();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getCache() completed");
			}
		}

	}

	@Override
	public Long getLastExecutedInstanceId(String batchName) {
		Long instanceId = 0L;
		List<Long> instanceIds;
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getLastInstanceId() started ");
		}
		try {
			instanceIds = getJobInstanceIds(batchName);
			if (CollectionUtils.isNotEmpty(instanceIds)) {
				Collections.sort(instanceIds);
				instanceId = instanceIds.get(instanceIds.size() - 1);
			}
			return instanceId;
		} finally {
			instanceIds = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getLastInstanceId() completed");
			}
		}
	}

	@Override
	public List<String> getAllBatchNamesLastExecution() {
		Long instanceId = 0L;
		List<String> batchList = new ArrayList<String>();
		JobExecution jobExecution = null;
		JobInstance jobInstance = null;
		DateFormat df = new SimpleDateFormat(TIMESTAMP_FORMAT);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getAllBatchNamesLastModified() started ");
		}
		try {
		  Set<String> allBatchNames = batchMetaStore.getAllBatchNames();
		 
	 	   for (String allBatchName : allBatchNames) {
				//Sathish Change To Store Batch Last Execution Date In Definition - Start
	 		  BatchDefinitionBean definitionBean= batchMetaStore.getBatchDefniton(allBatchName);
				//Sathish Change To Store Batch Last Execution Date In Definition - End

		    	instanceId = getLastExecutedInstanceId(allBatchName);
		    	if (LOGGER.isDebugEnabled()) {
			    LOGGER.debug("Examining the batch : " + allBatchName + " With ID : " + instanceId);
			}
			if (instanceId > 0) {
				jobInstance = getJobInstance(instanceId);
				if (jobInstance != null) {
				jobExecution = getLastJobExecution(jobInstance);
				if (jobExecution != null) {
					if(jobExecution.getStartTime()!=null){
						batchList.add(jobExecution.getJobInstance().getJobName()
								+ HASH_CHAR + df.format(jobExecution.getStartTime()));
					}
					else{
						batchList.add(allBatchName + HASH_CHAR + NEVER);
						if (LOGGER.isDebugEnabled()) {
						    LOGGER.debug("Adding the batch : " + allBatchName + " With Date : NULL TO NEVER");
						}
					}
				if (LOGGER.isDebugEnabled()) {
				    LOGGER.debug("Adding the batch : " + jobExecution.getJobInstance().getJobName() + " With Date : " + jobExecution.getStartTime());
				}
				} else {
				    LOGGER.warn("Invalid Job Execution encountered for Instance " + jobInstance.getId() + " during Search!");
				}
				} else {
				    LOGGER.warn("Invalid Job Instance encountered during Search!");
				}
			}//Sathish start
			else if(null!=definitionBean && null!=definitionBean.getParams().get("LASTEXECUTIONDATE")){
				batchList.add(allBatchName + HASH_CHAR + df.format(definitionBean.getParams().get("LASTEXECUTIONDATE")));
			}//Sathish end
			else {
				batchList.add(allBatchName + HASH_CHAR + NEVER);
				if (LOGGER.isDebugEnabled()) {
				    LOGGER.debug("Adding the batch : " + allBatchName + " With Date : Never");
				}
			}
		}
		return batchList;
		}
		finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getAllBatchNamesLastModified() completed");
			}
		}
	}
	
	public String getWarningForExecutionId(final Long jobExecutionId) {
		String message = null;
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getWarningForExecutionId() started ");
			}
			message = (String)getCache().getFromMap(BATCH_EXECUTION_WARNINGS, jobExecutionId);
			
			
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.getWarningForExecutionId() completed");
			}
		}
		return message;
	}
	

	public void removeWarningForExecutionId(final Long jobExecutionId) {
		
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.removeWarningForExecutionId() started ");
			}
			getCache().removeFromMap(BATCH_EXECUTION_WARNINGS, jobExecutionId);
									
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.removeWarningForExecutionId() completed");
			}
		}
		
	}

	@Override
	public void linkStartedJobIdtoNode(Long StartedJobId, String nodeName) {
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("SmartBatchRedisRuntimeStore.linkStartedJobIdtoNode Started");
	    }
	    try {

		getCache().appendTolistNoNs(
				concat(NODE_BATCH_STARTED_EXECUTIONS, nodeName),
				concat(ThreadContextUtils.getNamespace(), HYPHEN_CHAR,
					StartedJobId));
	     } finally {
		if (LOGGER.isTraceEnabled()) {
		    LOGGER.trace("SmartBatchRedisRuntimeStore.linkStartedJobIdtoNode Finished");
		}
	    }
	}

	@Override
	public void delinkStartedJobIdfromNode(Long startedJobId,
		String nodeName) {
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("SmartBatchRedisRuntimeStore.delinkStartedJobIdfromNode() started");
	}
	try {
		getCache().removeFromListNoNs(
				concat(NODE_BATCH_STARTED_EXECUTIONS, nodeName),
				-1,
				concat(ThreadContextUtils.getNamespace(), HYPHEN_CHAR,
						startedJobId));
	} finally {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.delinkStartedJobIdfromNode() completed");
		}
	}
	    
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getNodeStartedJobIds(String nodeName) {
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("SmartBatchRedisRuntimeStore.getNodeStartedJobIds() started");
	}
	try {
		return (List<String>) getCache().getListNoNs(
				concat(NODE_BATCH_STARTED_EXECUTIONS, nodeName));
	} finally {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.getNodeStartedJobIds() completed");
		}
	}
	}
}