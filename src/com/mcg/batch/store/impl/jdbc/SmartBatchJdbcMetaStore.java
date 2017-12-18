/**
 * 
 */
package com.mcg.batch.store.impl.jdbc;

import static com.mcg.batch.core.BatchWiringConstants.BATCH_JDBC_META_STORE_DATASOURCE;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_JDBC_META_TRANSACTION_MANAGER;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.mcg.batch.adapter.AdapterDefinitionBean;
import com.mcg.batch.core.support.BatchDefinitionBean;
import com.mcg.batch.store.BatchMetadataStore;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@SuppressWarnings("unused")
public class SmartBatchJdbcMetaStore implements BatchMetadataStore,
		InitializingBean {

	private static final String REGISTER_BATCH_DEFINITION = "INSERT INTO %PREFIX%JOB_DEFINITION (BATCH_NAME,BATCH_VERSION, BATCH_DEFN,BATCH_UI_DEFN,BATCH_PARAMS) VALUES (?,?,?,?,?)";
	private static final String MARK_OLD_BATCH_DEFINITION_INACTIVE = "UPDATE %PREFIX%JOB_DEFINITION  SET ACTIVE='N' WHERE BATCH_NAME=? AND BATCH_VERSION=?";
	private static final String GET_ALL_ACTIVE_BATCH_DEFINITIONS = "SELECT BATCH_NAME,BATCH_VERSION, BATCH_DEFN,BATCH_UI_DEFN,BATCH_PARAMS WHERE ACTIVE='Y'";
	private static final String GET_ACTIVE_BATCH_VERSION = "SELECT BATCH_VERSION WHERE WHERE BATCH_NAME=? AND BATCH_VERSION=? AND ACTIVE='Y'";

	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchJdbcMetaStore.class);

	private JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier(BATCH_JDBC_META_STORE_DATASOURCE)
	private DataSource dataSource;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#addAdapterBatchDependency
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public void addAdapterBatchDependency(String adapterName, String batchName) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#addProperties(java.lang.String
	 * , java.lang.String, java.lang.String)
	 */
	@Override
	public void addProperties(String property, String key, String value) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#getAdapter(java.lang.String)
	 */
	@Override
	public AdapterDefinitionBean getAdapter(String adapterName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchMetadataStore#getAllBatchNames()
	 */
	@Override
	public Set<String> getAllBatchNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#getAllDependednts(java.lang
	 * .String)
	 */
	@Override
	public Set<String> getAllDependednts(String batchName) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#getBatchDefniton(java.lang
	 * .String)
	 */
	@Override
	public BatchDefinitionBean getBatchDefniton(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#getBatchVersion(java.lang
	 * .String)
	 */
	@Override
	public int getBatchVersion(String batchName) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#getProperty(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String getProperty(String propertyName, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#getPropertyNames(java.lang
	 * .String)
	 */
	@Override
	public Set<String> getPropertyKeys(String property) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchMetadataStore#getReisteredBatchNames()
	 */
	@Override
	public Set<String> getReisteredBatchNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#isBatchReistered(java.lang
	 * .String)
	 */
	@Override
	public boolean isBatchReistered(String batchName) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchMetadataStore#registerAdapter(com.scb.
	 * smartbatch.adapter.AdapterDefinitionBean)
	 */
	@Override
	public void registerAdapter(AdapterDefinitionBean adapterDefnbean) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#registerBatch(com.mcg.batch
	 * .core.support.BatchDefinitionBean)
	 */
	@Override
	@Transactional(BATCH_JDBC_META_TRANSACTION_MANAGER)
	public void registerBatch(BatchDefinitionBean bean) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchJdbcMetaStore.registerBatch() started");
		}
		Object[] params = null;
		int[] paramType = null;

		try {
			throw new UnsupportedOperationException();

			// jdbcTemplate.update()

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchJdbcMetaStore.registerBatch() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#removeAdapter(java.lang.String
	 * )
	 */
	@Override
	public void removeAdapter(String adapterName) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchMetadataStore#getAllAdapterNames()
	 */
	@Override
	public Set<String> getAllAdapterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#unRegisterBatch(java.lang
	 * .String, boolean)
	 */
	@Override
	public void unRegisterBatch(String batchName, boolean remove) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#registerBatch(java.lang.String
	 * )
	 */
	@Override
	public void reRegisterBatch(String batchName) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#removePropertyKey(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void removePropertyKey(String property, String key) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#removeAllPropertyKeys(java
	 * .lang.String)
	 */
	@Override
	public void removeAllPropertyKeys(String property) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchMetadataStore#getAllPropertyNames()
	 */
	@Override
	public Set<String> getAllPropertyNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#getPropertyDetails(java.lang
	 * .String)
	 */
	@Override
	public HashMap<String, String> getPropertyDetails(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.mcg.batch.store.BatchMetadataStore#getTotalBatchsRegistered()
	 */
	@Override
	public Set<String> getTotalBatchesRegistered() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveEntriesToCache(String mapName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveAllAdaptersToDomainBasedCache() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getAllBatchesfrmAdapter(String adapter) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void addBatchAdapterDependency(String batchName, String adapterName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeBatchfrmAdapterKey(String adapterName, String batchvalue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadAllBatchesdepenAdapters() {
		// TODO Auto-generated method stub
		
	}


}
