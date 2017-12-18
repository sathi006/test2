/**
 * 
 */
package com.mcg.batch.store.impl.jdbc;

import static com.mcg.batch.core.BatchWiringConstants.BATCH_JDBC_RT_STORE_DATASOURCE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.dao.JdbcExecutionContextDao;
import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JdbcJobInstanceDao;
import org.springframework.batch.core.repository.dao.JdbcStepExecutionDao;
import org.springframework.batch.core.repository.dao.XStreamExecutionContextStringSerializer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.mcg.batch.exceptions.BatchException;
import com.mcg.batch.store.ArchivalInstanceDetails;
import com.mcg.batch.store.BatchRuntimeStore;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class SmartBatchJdbcRTStore implements BatchRuntimeStore,
		InitializingBean {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchJdbcRTStore.class);

	private JdbcExecutionContextDao jdbcExecutionContextDao;
	private JdbcJobExecutionDao jdbcJobExecutionDao;
	private JdbcJobInstanceDao jdbcJobInstanceDao;
	private JdbcStepExecutionDao jdbcStepExecutionDao;
	private BatchNodeJobDao batchNodeJobDao;

	@Autowired
	@Qualifier(BATCH_JDBC_RT_STORE_DATASOURCE)
	private DataSource dataSource;

	private JdbcOperations jdbcTemplate;

	private DataFieldMaxValueIncrementerFactory incrementerFactory = null;
	private String databaseType = null;
	private String tablePrefix = "BATCH_";
	private XStreamExecutionContextStringSerializer serializer;

	/**
	 * prevent external instantiation
	 */
	private SmartBatchJdbcRTStore() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.dao.ExecutionContextDao#
	 * getExecutionContext(org.springframework.batch.core.JobExecution)
	 */
	@Override
	public ExecutionContext getExecutionContext(JobExecution jobExecution) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.getExecutionContext() started");
		}
		try {
			return jdbcExecutionContextDao.getExecutionContext(jobExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getExecutionContext() completed");
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
	public ExecutionContext getExecutionContext(StepExecution stepExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.getExecutionContext() started");
		}
		try {
			return jdbcExecutionContextDao.getExecutionContext(stepExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getExecutionContext() completed");
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
	public void saveExecutionContext(JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.saveExecutionContext() started");
		}
		try {
			jdbcExecutionContextDao.saveExecutionContext(jobExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.saveExecutionContext() completed");
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
	public void saveExecutionContext(StepExecution stepExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.saveExecutionContext() started");
		}
		try {
			jdbcExecutionContextDao.saveExecutionContext(stepExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.saveExecutionContext() completed");
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
	public void saveExecutionContexts(Collection<StepExecution> stepExecutions) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.saveExecutionContexts() started");
		}
		try {
			jdbcExecutionContextDao.saveExecutionContexts(stepExecutions);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.saveExecutionContexts() completed");
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
	public void updateExecutionContext(JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.updateExecutionContext() started");
		}
		try {
			jdbcExecutionContextDao.updateExecutionContext(jobExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.updateExecutionContext() completed");
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
	public void updateExecutionContext(StepExecution stepExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.updateExecutionContext() started");
		}
		try {
			jdbcExecutionContextDao.updateExecutionContext(stepExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.updateExecutionContext() completed");
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
	public List<JobExecution> findJobExecutions(JobInstance jobInstance) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.findJobExecutions() started");
		}
		try {
			return jdbcJobExecutionDao.findJobExecutions(jobInstance);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.findJobExecutions() completed");
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
			LOGGER.trace("SmartBatchJdbcRTStore.findRunningJobExecutions() started");
		}
		try {
			return jdbcJobExecutionDao.findRunningJobExecutions(jobName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.findRunningJobExecutions() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.repository.dao.JobExecutionDao#getJobExecution
	 * (java.lang.Long)
	 */
	@Override
	public JobExecution getJobExecution(Long jobExecutionId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.getJobExecution() started");
		}
		try {
			return jdbcJobExecutionDao.getJobExecution(jobExecutionId);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getJobExecution() completed");
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
			LOGGER.trace("SmartBatchJdbcRTStore.getLastJobExecution() started");
		}
		try {
			return jdbcJobExecutionDao.getLastJobExecution(jobInstance);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getLastJobExecution() completed");
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
			LOGGER.trace("SmartBatchJdbcRTStore.saveJobExecution() started");
		}
		try {
			jdbcJobExecutionDao.saveJobExecution(jobExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.saveJobExecution() completed");
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
	public void synchronizeStatus(JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.synchronizeStatus() started");
		}
		try {
			jdbcJobExecutionDao.synchronizeStatus(jobExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.synchronizeStatus() completed");
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
	public void updateJobExecution(JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.updateJobExecution() started");
		}
		try {
			jdbcJobExecutionDao.updateJobExecution(jobExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.updateJobExecution() completed");
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
	public JobInstance createJobInstance(String jobName,
			JobParameters jobParameters) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.createJobInstance() started");
		}
		try {
			return jdbcJobInstanceDao.createJobInstance(jobName, jobParameters);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.createJobInstance() completed");
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
	public JobInstance getJobInstance(JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.getJobInstance() started");
		}
		try {
			return jdbcJobInstanceDao.getJobInstance(jobExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getJobInstance() completed");
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
	public JobInstance getJobInstance(Long jobInstanceId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.getJobInstance() started");
		}
		try {
			return jdbcJobInstanceDao.getJobInstance(jobInstanceId);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getJobInstance() completed");
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
			LOGGER.trace("SmartBatchJdbcRTStore.getJobInstance() started");
		}
		try {
			return jdbcJobInstanceDao.getJobInstance(jobName, jobParameters);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getJobInstance() completed");
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
			LOGGER.trace("SmartBatchJdbcRTStore.getJobInstanceCount() started");
		}
		try {
			return jdbcJobInstanceDao.getJobInstanceCount(jobName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getJobInstanceCount() completed");
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
	public List<JobInstance> getJobInstances(String jobName, int start,
			int count) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.getJobInstances() started");
		}
		try {
			return jdbcJobInstanceDao.getJobInstances(jobName, start, count);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getJobInstances() completed");
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
	@Override
	public List<String> getJobNames() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.getJobNames() started");
		}
		try {
			return jdbcJobInstanceDao.getJobNames();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getJobNames() completed");
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
	public void addStepExecutions(JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.addStepExecutions() started");
		}
		try {
			jdbcStepExecutionDao.addStepExecutions(jobExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.addStepExecutions() completed");
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
	public StepExecution getStepExecution(JobExecution jobExecution,
			Long stepExecutionId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.getStepExecution() started");
		}
		try {
			return jdbcStepExecutionDao.getStepExecution(jobExecution,
					stepExecutionId);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getStepExecution() completed");
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
	public void saveStepExecution(StepExecution stepExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.saveStepExecution() started");
		}
		try {
			jdbcStepExecutionDao.saveStepExecution(stepExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.saveStepExecution() completed");
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
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.saveStepExecutions() started");
		}
		try {
			jdbcStepExecutionDao.saveStepExecutions(stepExecutions);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.saveStepExecutions() completed");
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
	public void updateStepExecution(StepExecution stepExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.updateStepExecution() started");
		}
		try {
			jdbcStepExecutionDao.updateStepExecution(stepExecution);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.updateStepExecution() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchRuntimeStore#deLinkNodeStartedJobs(java
	 * .lang.Long)
	 */
	@Override
	public void deLinkNodeStartedJobs(Long jobExecutionId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.deLinkNodeStartedJobs() started");
		}
		try {
			batchNodeJobDao.deLinkNodeStartedJobs(jobExecutionId);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.deLinkNodeStartedJobs() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchRuntimeStore#getNodeStartedJobIds()
	 */
	@Override
	public List<String> getNodeStartedJobIds() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.getNodeStartedJobIds() started");
		}
		try {
			return batchNodeJobDao.getNodeStartedJobIds();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getNodeStartedJobIds() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchRuntimeStore#linkNodeStartedJobs(java.lang
	 * .Long)
	 */
	@Override
	public void linkNodeStartedJobs(Long jobExecutionId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.linkNodeStartedJobs() started");
		}
		try {
			batchNodeJobDao.linkNodeStartedJobs(jobExecutionId);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.linkNodeStartedJobs() completed");
			}
		}
	}

	private int determineClobTypeToUse(String databaseType) {
		if (DatabaseType.SYBASE == DatabaseType.valueOf(databaseType
				.toUpperCase())) {
			return -1;
		}

		return 2005;
	}

	private JdbcJobInstanceDao createJobInstanceDao() throws Exception {
		JdbcJobInstanceDao dao = new JdbcJobInstanceDao();
		dao.setJdbcTemplate(this.jdbcTemplate);
		dao.setJobIncrementer(this.incrementerFactory.getIncrementer(
				this.databaseType, this.tablePrefix + "JOB_SEQ"));
		dao.setTablePrefix(this.tablePrefix);
		dao.afterPropertiesSet();
		return dao;
	}

	private JdbcJobExecutionDao createJobExecutionDao() throws Exception {
		JdbcJobExecutionDao dao = new JdbcJobExecutionDao();
		dao.setJdbcTemplate(this.jdbcTemplate);
		dao.setJobExecutionIncrementer(this.incrementerFactory.getIncrementer(
				this.databaseType, this.tablePrefix + "JOB_EXECUTION_SEQ"));
		dao.setTablePrefix(this.tablePrefix);
		dao.setClobTypeToUse(determineClobTypeToUse(this.databaseType));
		dao.afterPropertiesSet();
		return dao;
	}

	private JdbcStepExecutionDao createStepExecutionDao() throws Exception {
		JdbcStepExecutionDao dao = new JdbcStepExecutionDao();
		dao.setJdbcTemplate(this.jdbcTemplate);
		dao.setStepExecutionIncrementer(this.incrementerFactory.getIncrementer(
				this.databaseType, this.tablePrefix + "STEP_EXECUTION_SEQ"));

		dao.setTablePrefix(this.tablePrefix);
		dao.setClobTypeToUse(determineClobTypeToUse(this.databaseType));
		dao.afterPropertiesSet();
		return dao;
	}

	private JdbcExecutionContextDao createExecutionContextDao()
			throws Exception {
		JdbcExecutionContextDao dao = new JdbcExecutionContextDao();
		dao.setJdbcTemplate(this.jdbcTemplate);
		dao.setTablePrefix(this.tablePrefix);
		dao.setClobTypeToUse(determineClobTypeToUse(this.databaseType));
		dao.setSerializer(this.serializer);
		dao.afterPropertiesSet();
		return dao;
	}

	private BatchNodeJobDao createBatchNodeJobDao() throws Exception {
		BatchNodeJobDao dao = new BatchNodeJobDao();
		dao.setJdbcTemplate(this.jdbcTemplate);
		dao.setTablePrefix(this.tablePrefix);
		dao.setClobTypeToUse(determineClobTypeToUse(this.databaseType));
		dao.afterPropertiesSet();
		return dao;
	}

	/**
	 * @return the tablePrefix String
	 */
	public String getTablePrefix() {
		return tablePrefix;
	}

	/**
	 * @param tablePrefix
	 *            String
	 */
	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.dataSource, "DataSource must not be null.");

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.afterPropertiesSet() started");
		}

		try {
			this.jdbcTemplate = new JdbcTemplate(this.dataSource);
			this.incrementerFactory = new DefaultDataFieldMaxValueIncrementerFactory(
					this.dataSource);
			this.databaseType = DatabaseType.fromMetaData(this.dataSource)
					.name();
			this.serializer = new XStreamExecutionContextStringSerializer();
			this.serializer.afterPropertiesSet();
			jdbcExecutionContextDao = createExecutionContextDao();
			jdbcJobExecutionDao = createJobExecutionDao();
			jdbcJobInstanceDao = createJobInstanceDao();
			jdbcStepExecutionDao = createStepExecutionDao();
			this.batchNodeJobDao = createBatchNodeJobDao();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.afterPropertiesSet() completed");
			}
		}

	}

	
	public Long archive(ArrayList<String> batchNames, Long startEpoch,
			Long endEpoch, String descripton, String type)
			throws BatchException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchRuntimeStore#archive(java.util.ArrayList,
	 * java.lang.Long, java.lang.Long)
	 */


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchRuntimeStore#restoreFromArchive(java.lang
	 * .String)
	 */
	@Override
	public Long restoreFromArchive(String dumpFile) throws BatchException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchRuntimeStore#getArchivalInstances()
	 */
	@Override
	public Set<Long> getArchivalInstances() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchRuntimeStore#getJobExecutionsIds(java.lang
	 * .Long)
	 */
	@Override
	public List<Long> getJobExecutionsIds(Long jobInstanceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllBatchNamesLastExecution() {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public Long getLastExecutedInstanceId(String batchName) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public String getWarningForExecutionId(Long jobExecutionId) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public void removeWarningForExecutionId(Long jobExecutionId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getNodeStartedJobIds(String nodeName) {
	    // TODO Auto-generated method stub
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.getNodeStartedJobIds() started");
		}
		try {
			return batchNodeJobDao.getNodeStartedJobIds(nodeName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcRTStore.getNodeStartedJobIds() completed");
			}
		}
	}

	@Override
	public void linkStartedJobIdtoNode(Long StartedJobId, String nodeName) {
	    // TODO Auto-generated method stub
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("SmartBatchJdbcRTStore.linkStartedJobIdtoNode() started");
	}
	try {
		batchNodeJobDao.linkStartedJobtoNode(StartedJobId, nodeName);
	} finally {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.linkStartedJobIdtoNode() completed");
		}
	}
	}

	@Override
	public void delinkStartedJobIdfromNode(Long startedJobId,
		String nodeName) {
	    // TODO Auto-generated method stub
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("SmartBatchJdbcRTStore.delinkStartedJobIdfromNode() started");
	}
	try {
		batchNodeJobDao.deLinkNodeStartedJobs(startedJobId, nodeName);
	} finally {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcRTStore.delinkStartedJobIdfromNode() completed");
		}
	}
	}



}
