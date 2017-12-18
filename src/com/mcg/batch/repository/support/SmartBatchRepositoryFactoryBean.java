/**
 * 
 */
package com.mcg.batch.repository.support;

import static com.mcg.batch.core.BatchWiringConstants.BATCH_RT_STORE_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_REPOSITORY_FACTORY_COMPONENT;

import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.core.repository.support.AbstractJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.mcg.batch.store.BatchRuntimeStore;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@Component(SMART_BATCH_REPOSITORY_FACTORY_COMPONENT)
public class SmartBatchRepositoryFactoryBean extends
		AbstractJobRepositoryFactoryBean {

	@Autowired(required = true)
	@Qualifier(BATCH_RT_STORE_COMPONENT)
	private BatchRuntimeStore batchStore;

	/**
	 * prevent external instantiation
	 */
	private SmartBatchRepositoryFactoryBean() {
		setTransactionManager(new ResourcelessTransactionManager());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.support.
	 * AbstractJobRepositoryFactoryBean#createExecutionContextDao()
	 */
	@Override
	protected ExecutionContextDao createExecutionContextDao() throws Exception {

		return batchStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.support.
	 * AbstractJobRepositoryFactoryBean#createJobExecutionDao()
	 */
	@Override
	protected JobExecutionDao createJobExecutionDao() throws Exception {
		return batchStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.support.
	 * AbstractJobRepositoryFactoryBean#createJobInstanceDao()
	 */
	@Override
	protected JobInstanceDao createJobInstanceDao() throws Exception {
		return batchStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.repository.support.
	 * AbstractJobRepositoryFactoryBean#createStepExecutionDao()
	 */
	@Override
	protected StepExecutionDao createStepExecutionDao() throws Exception {
		return batchStore;
	}
}
