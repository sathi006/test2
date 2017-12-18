/**
 * 
 */
package com.mcg.batch.store.impl.redis;

import static com.mcg.batch.utils.CollectionUtils.isEmpty;
import static com.mcg.batch.utils.CollectionUtils.isNotEmpty;
import static com.mcg.batch.utils.StringHelper.concat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.mcg.batch.adapter.AdapterDefinitionBean;
import com.mcg.batch.cache.SmartBatchCache;
import com.mcg.batch.cache.SmartBatchCacheFactory;
import com.mcg.batch.cache.impl.RedisCacheFactory;
import com.mcg.batch.core.support.BatchDefinitionBean;
import com.mcg.batch.store.BatchMetadataStore;
import com.mcg.batch.utils.CollectionUtils;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class SmartBatchRedisMetaStore implements BatchMetadataStore {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchRedisMetaStore.class);
	private static final SmartBatchCacheFactory CACHE_FACTORY = RedisCacheFactory
			.getInstance();

	/**
	 * A key for holding the registered properties list
	 */
	private static final String REGISTERED_PROPERTIES = "PROPERTIES";

	private static final String PROPERTIES_PREFIX = "PROPERTY_";

	/**
	 * A Key prefix to maintain the dependency of adapters by Batch.
	 */
	public static final String BATCH_ADAPTER_DEPENDENCY_PREFIX = "BATCH_ADAPTER_DEPENDENCY_";
	/**
	 * A Key prefix to maintain the dependency of batch by adapter.
	 */
	public static final String ADAPTER_BATCH_DEPENDENCY_PREFIX = "ADAPTER_BATCH_DEPENDENCY_";
	/**
	 * This is a list of all BatchBeans registered.
	 */
	public static final String BATCH_JOBS_DEFINITION = "BATCH_JOBS_DEFINITION";
	/**
	 * A key for all registered Batches.
	 */
	public static final String BATCH_REGISTERED_NAMES = "BATCH_REGISTERED_CACHE";
	/**
	 * A key for all registered Batches till date. This batch name will never be
	 * removed from this list.
	 */
	public static final String ALL_BATCH_REGISTERED_NAMES = "ALL_BATCH_REGISTERED_CACHE";
	/**
	 * A Key to access all the adapter names.
	 */
	public static final String REGISTERED_ADAPTER_NAMES = "REGISTERED_ADAPTER_NAMES";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#getBatchVersion(java.lang
	 * .String)
	 */
	@Override
	public int getBatchVersion(String batchName) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchCacheStore.getBatchVersion() started ");
		}
		int batchVersion = 0;
		BatchDefinitionBean bean = null;
		try {
			bean = getBatchDefniton(batchName);
			if (bean != null) {
				batchVersion = bean.getVersion();
			}

			return batchVersion;
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchCacheStore.getBatchVersion() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchMetaDataStore#getBatchNames()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getReisteredBatchNames() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchCacheStore.getReisteredBatchNames() started ");
		}
		try {
			return (Set<String>) getCache().getAllKeys(BATCH_REGISTERED_NAMES);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchCacheStore.getReisteredBatchNames() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#getTotalBatchsRegistered()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getTotalBatchesRegistered() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchCacheStore.getReisteredBatchNames() started ");
		}
		try {
			return (Set<String>) getCache().getAllKeys(
					ALL_BATCH_REGISTERED_NAMES);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchCacheStore.getReisteredBatchNames() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchMetaDataStore#getAllBatchNames()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getAllBatchNames() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchCacheStore.getAllBatchNames() started ");
		}
		try {
			return (Set<String>) getCache().getAllKeys(BATCH_JOBS_DEFINITION);

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchCacheStore.getAllBatchNames() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#getBatchDefniton(java.lang
	 * .String)
	 */
	@Override
	public BatchDefinitionBean getBatchDefniton(final String batchname) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchCacheStore.getBatchDefniton() started ");
		}

		try {
			Assert.notNull(batchname, "Name shoud not be null");
			return (BatchDefinitionBean) getCache().getFromMap(
					BATCH_JOBS_DEFINITION, batchname);
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchCacheStore.getBatchDefniton() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#registerBatch(com.mcg.batch
	 * . core.support.BatchDefinitionBean)
	 */
	@Override
	public void registerBatch(BatchDefinitionBean bean) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchCacheStore.registerBatch() started ");
		}
		Assert.notNull(bean, "The Batch Definition bean cannot be null");
		Assert.notNull(bean.getBatchName(),
				"The Batch Name bean cannot be null");
		try {
			getCache().putToMap(BATCH_JOBS_DEFINITION, bean.getBatchName(),
					bean);
			getCache().putToMap(BATCH_REGISTERED_NAMES, bean.getBatchName(),
					System.currentTimeMillis());
			getCache().putIfAbsentToMap(ALL_BATCH_REGISTERED_NAMES,
					bean.getBatchName(), System.currentTimeMillis());
			
			String dependentAdapters[]= bean.getdependentAdapters();
			for(int i=0;i<dependentAdapters.length;i++){
			addBatchAdapterDependency(bean.getBatchName(), dependentAdapters[i]);
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchCacheStore.registerBatch() completed");
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#isBatchReistered(java.lang
	 * .String)
	 */
	@Override
	public boolean isBatchReistered(String batchName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisCacheStore.isBatchReistered() started ");
		}
		Assert.notNull(batchName, "Batch Name cannot be null");
		try {
			return getCache().hasKeyInMap(BATCH_REGISTERED_NAMES, batchName)
					&& getCache().hasKeyInMap(BATCH_JOBS_DEFINITION, batchName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisCacheStore.isBatchReistered() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#unRegisterBatch(java.lang
	 * .String)
	 */
	@Override
	public void unRegisterBatch(String batchName, boolean remove) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisCacheStore.unRegisterBatch() started ");
		}
		Assert.notNull(batchName, "Batch name cannot be null");
		try {
			getCache().removeFromMap(BATCH_REGISTERED_NAMES, batchName);
			if (remove) {
				BatchDefinitionBean bean=getBatchDefniton(batchName);
				String dependentAdapters[]= bean.getdependentAdapters();
				for(int i=0;i<dependentAdapters.length;i++){
				removeBatchfrmAdapterKey(dependentAdapters[i], batchName);
				}
				getCache().removeFromMap(BATCH_JOBS_DEFINITION, batchName);
			}
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisCacheStore.unRegisterBatch() completed");
			}
		}
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#removeBatchfrmAdapterKey(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void removeBatchfrmAdapterKey(String adapterName, String batchvalue) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisMetaStore.removeBatchfrmAdapterKey() started");
		}
		try {	
				getCache().removeFromList(concat(ADAPTER_BATCH_DEPENDENCY_PREFIX, adapterName), 0,
						batchvalue);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisMetaStore.removeBatchfrmAdapterKey() completed");
			}
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#addProperties(java.lang.String
	 * , java.lang.String, java.lang.String)
	 */
	@Override
	public void addProperties(String propertyName, String key, String value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchCacheStore.addProperties() started ");
		}
		try {

			getCache().putToMap(concat(PROPERTIES_PREFIX, propertyName), key,
					value);
			getCache().appendTolist(REGISTERED_PROPERTIES,
					concat(PROPERTIES_PREFIX, propertyName));
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchCacheStore.addProperties() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#getProperty(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String getProperty(String propertyName, String key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchCacheStore.getProperty() started ");
		}
		try {
			return (String) getCache().getFromMap(
					concat(PROPERTIES_PREFIX, propertyName), key);
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchCacheStore.getProperty() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#getPropertyNames(java.lang
	 * .String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getPropertyKeys(String propertyName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchCacheStore.getPropertyNames() started ");
		}
		try {
			return (Set<String>) getCache().getAllKeys(
					concat(PROPERTIES_PREFIX, propertyName));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchCacheStore.getPropertyNames() completed");
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#reRegisterBatch(java.lang
	 * .String )
	 */
	@Override
	public void reRegisterBatch(String batchName) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisMetaStore.registerBatch() started");
		}
		try {
			if (getCache().hasKeyInMap(BATCH_JOBS_DEFINITION, batchName)) {
				getCache().putToMap(BATCH_REGISTERED_NAMES, batchName,
						System.currentTimeMillis());
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisMetaStore.registerBatch() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#removeAllPropertyKeys(java
	 * .lang.String)
	 */
	@Override
	public void removeAllPropertyKeys(final String propertyName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisMetaStore.removeAllPropertyKeys() started");
		}
		Set<String> keys = null;
		try {

			keys = getPropertyKeys(propertyName);
			if (isNotEmpty(keys)) {
				for (String key : keys) {
					getCache().removeFromMap(
							concat(PROPERTIES_PREFIX, propertyName), key);
					key = null;
				}
				getCache().removeFromList(REGISTERED_PROPERTIES, 0,
						concat(PROPERTIES_PREFIX, propertyName));
			}

		} finally {
			keys = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisMetaStore.removeAllPropertyKeys() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetadataStore#removePropertyKey(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void removePropertyKey(String propertyName, String key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisMetaStore.removePropertyKey() started");
		}
		try {
			getCache().removeFromMap(concat(PROPERTIES_PREFIX, propertyName),
					key);
			if (isEmpty(getPropertyKeys(propertyName))) {
				getCache().removeFromList(REGISTERED_PROPERTIES, 0,
						concat(PROPERTIES_PREFIX, propertyName));
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisMetaStore.removePropertyKey() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchMetadataStore#getAllPropertyNames()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getAllPropertyNames() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisMetaStore.getAllPropertyNames() started");
		}

		List<String> registerdPropList = null;
		Set<String> props = new HashSet<String>();
		try {
			registerdPropList = (List<String>) getCache().getList(
					REGISTERED_PROPERTIES);
			if (CollectionUtils.isNotEmpty(registerdPropList)) {
				for (String registeredProp : registerdPropList) {
					props.add(registeredProp.substring(REGISTERED_PROPERTIES
							.length() - 1));
				}

			}
			return props;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisMetaStore.getAllPropertyNames() completed");
			}
		}
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

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisMetaStore.getPropertyDetails() started");
		}
		HashMap<String, String> properties = new HashMap<String, String>();
		Set<String> keys = null;
		try {

			keys = getPropertyKeys(propertyName);
			if (isNotEmpty(keys)) {

				for (String key : keys) {
					properties.put(key, getProperty(propertyName, key));
					key = null;
				}
			}
			return properties;
		} finally {
			keys = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisMetaStore.getPropertyDetails() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#getAdapter(java.lang.String)
	 */
	@Override
	public AdapterDefinitionBean getAdapter(String adapterName) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisCacheStore.getAdapter() started");
		}
		try {
			/*return (AdapterDefinitionBean) getCache().getFromMap(
					REGISTERED_ADAPTER_NAMES, adapterName);*/
			return (AdapterDefinitionBean) getCache().getFromMapNoNS(
					REGISTERED_ADAPTER_NAMES, adapterName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisCacheStore.getAdapter() completed");
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
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisMetaStore.removeAdapter() started");
		}
		try {
			getCache().removeFromMapNoNS(REGISTERED_ADAPTER_NAMES, adapterName);
			/*getCache().removeFromMap(REGISTERED_ADAPTER_NAMES, adapterName);*/
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisMetaStore.removeAdapter() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchMetadataStore#getAllAdapterNames()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getAllAdapterNames() {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisMetaStore.getAllAdapterNames() started");
		}
		try {
			return (Set<String>) getCache()
					.getAllKeysNoNS(REGISTERED_ADAPTER_NAMES);
			/*return (Set<String>) getCache()
					.getAllKeys(REGISTERED_ADAPTER_NAMES);*/

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisMetaStore.getAllAdapterNames() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.BatchMetaDataStore#registerAdapter(com.scb.
	 * smartbatch .adapter.AdapterDefinitionBean)
	 */
	@Override
	public void registerAdapter(AdapterDefinitionBean adapterDefnbean) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisCacheStore.registerAdapter() started");
		}
		try {
			getCache().putToMapNoNS(REGISTERED_ADAPTER_NAMES,
					adapterDefnbean.getAdapterName(), adapterDefnbean);
			
			/*getCache().putToMap(REGISTERED_ADAPTER_NAMES,
					adapterDefnbean.getAdapterName(), adapterDefnbean);*/
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisCacheStore.registerAdapter() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#addBatchDependency(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void addAdapterBatchDependency(String adapterName, String batchName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisCacheStore.addBatchDependency() started");
		}
		try {
			getCache().appendTolist(
					concat(BATCH_ADAPTER_DEPENDENCY_PREFIX, batchName),
					adapterName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisCacheStore.addBatchDependency() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.store.BatchMetaDataStore#getAllDependednts(java.lang
	 * .String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getAllDependednts(String batchName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisCacheStore.getAllDependednts() started");
		}
		List<String> dependentsList;
		HashSet<String> dependents = new HashSet<String>();
		try {
			dependentsList = Arrays.asList(getBatchDefniton(batchName).getdependentAdapters());
			if (dependentsList != null) {
				dependents.addAll(dependentsList);
			}
			return dependents;
		} finally {
			dependentsList = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisCacheStore.getAllDependednts() completed");
			}
		}
	}

	private static final SmartBatchCache getCache() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisCacheStore.getCache() started");
		}

		try {
			return CACHE_FACTORY.getCache();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisCacheStore.getCache() completed");
			}
		}

	}
	
	
	public void removeAdapterAndAdd(String adapterName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisMetaStore.removeAdapter() started");
		}
		try {
			getCache().removeFromMapNoNS(REGISTERED_ADAPTER_NAMES, adapterName);
			 /*getCache().removeFromMap(REGISTERED_ADAPTER_NAMES, adapterName);*/
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisMetaStore.removeAdapter() completed");
			}
		}
	}	
		/* (non-Javadoc)
		 * @see com.mcg.batch.store.BatchMetadataStore#moveEntriesToCache(java.lang.String)
		 */
		@SuppressWarnings("unchecked")
		public void moveEntriesToCache(String mapName){
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisMetaStore.moveEntriesToCache() started");
			}
			Set<String> key;
			try {
				key = (Set<String>) getCache().getAllKeys(mapName);
				 for (String keyName : key) {	 
					 getCache().putToMapNoNS(mapName,
							 keyName, getCache().getFromMap(
									 mapName, keyName));
					// getCache().removeFromMap(mapName, keyName);
				}
			} finally {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("SmartBatchRedisMetaStore.moveEntriesToCache() completed");
				}
			}
	}
		@SuppressWarnings("unchecked")
	       public void moveAllAdaptersToDomainBasedCache() {
	           if (LOGGER.isDebugEnabled()) {
	              LOGGER.debug("SmartBatchRedisMetaStore.moveAllAdaptersToDomainBasedCache Started");
	           }
	           Set<String> jobNamesinDomain = null;
	           BatchDefinitionBean batch = null;
	           String[] dependentAdapters = null;
	           AdapterDefinitionBean adapterDefinitionBean =null;
	           try {
	              jobNamesinDomain = (Set<String>) getCache().getAllKeys(BATCH_REGISTERED_NAMES);
	              if(LOGGER.isDebugEnabled()){
	            	  LOGGER.debug("jobNamesinDomain : " + jobNamesinDomain);
	              }
	              if (jobNamesinDomain != null) {
	                  for (String jobName : jobNamesinDomain) {
	                     batch = (BatchDefinitionBean) getCache().getFromMap(BATCH_JOBS_DEFINITION, jobName);
	                     if (batch != null) { 
	                         dependentAdapters = batch.getdependentAdapters();
	                         if(LOGGER.isDebugEnabled()){
		   	   	            	  LOGGER.debug("batch : " + batch+" dependent Adapter : "+dependentAdapters);
		   	   	              }
	                         if (dependentAdapters != null) {
	                        	 
	                           for (String adapter : dependentAdapters) {  
	                        	   if(LOGGER.isDebugEnabled()){
		 	                              LOGGER.debug("adapter : " + adapter);
		 	                        	 }
	                        	   adapterDefinitionBean =  (AdapterDefinitionBean)getCache().getFromMapNoNS(REGISTERED_ADAPTER_NAMES, adapter);
	                        	   if(adapterDefinitionBean != null){
	                               getCache().putToMap(REGISTERED_ADAPTER_NAMES, adapter,
	                                      getCache().getFromMapNoNS(REGISTERED_ADAPTER_NAMES, adapter));
	                        	   }
	                           }
	                         } else {
	                        	 if(LOGGER.isDebugEnabled()){
	                              LOGGER.debug("No dependent adapters found for batch " + jobName);
	                        	 }
	                         }
	                     } else {
	                    	 if(LOGGER.isDebugEnabled()){
	                         LOGGER.debug("No definition found for batch " + jobName);
	                    	 }
	                     }
	                  }
	              } else {
	            	  if(LOGGER.isDebugEnabled()){
	                  LOGGER.debug("No batches found in domain " + ThreadContextUtils.getNamespace());
	            	  }
	              }
	           } finally {
	              if (LOGGER.isDebugEnabled()) {
	                  LOGGER.debug("SmartBatchRedisMetaStore.moveAllAdaptersToDomainBasedCache Finished");
	              }
	           }
	       }
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.mcg.batch.store.BatchMetadataStore#getAllBatchesfrmAdapter(java.lang
		 * .String, java.lang.String)
		 */
		@SuppressWarnings({ "unchecked" })
		@Override
		public Set<String> getAllBatchesfrmAdapter(String adapter) {
			// TODO Auto-generated method stub

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisCacheStore.getAllBatchesfrmAdapter() started");
			}
			List<String> dependentsList;
			HashSet<String> dependents = new HashSet<String>();
			try {
				
				//LOGGER.debug("2 getAllBatchesfrmAdapter Adapter name-->"+adapter);
				
				dependentsList = (List<String>) getCache().getList(
						concat(ADAPTER_BATCH_DEPENDENCY_PREFIX, adapter));
				
				//LOGGER.debug("3 getAllBatchesfrmAdapter Batch list:"+dependentsList);
				if (dependentsList != null) {
					dependents.addAll(dependentsList);
				}
				
				//LOGGER.debug("From services-->"+dependents);
				return dependents;
				

				
			} finally {
				dependentsList = null;
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("SmartBatchRedisCacheStore.getAllBatchesfrmAdapter() completed");
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.mcg.batch.store.BatchMetaDataStore#addAdapterDependency(java.lang
		 * .String, java.lang.String)
		 */		
		@Override
		public void addBatchAdapterDependency(String batchName,
				String adapterName) {
			// TODO Auto-generated method stub
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisCacheStore.addBatchAdapterDependency() started");
			}
			try {
				getCache().appendTolist(
						concat(ADAPTER_BATCH_DEPENDENCY_PREFIX, adapterName),
						batchName);
				
				Set<String> tempSet= getAllBatchesfrmAdapter(adapterName);
				LOGGER.debug("Values-->"+tempSet);
				
			} finally {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("SmartBatchRedisCacheStore.addBatchAdapterDependency() completed");
				}
			}
			
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.mcg.batch.store.BatchMetaDataStore#loadAllBatchesdepenAdaptersy()
		 */	
		@Override
		public void loadAllBatchesdepenAdapters() {
			Set<String> allbatch=getAllBatchNames();	
			Set<String> allDepenAdapters;
			Iterator<String> batchIterator=allbatch.iterator();
			while(batchIterator.hasNext()){
				String batchname=batchIterator.next();
				allDepenAdapters=getAllDependednts(batchname);
			if(allDepenAdapters.size()>0){
					Iterator<String> adapterIterator=allDepenAdapters.iterator();
					while(adapterIterator.hasNext()){
						String adaptername=adapterIterator.next();
						Set<String> allDepenBatches=getAllBatchesfrmAdapter(adaptername);
						if(!(allDepenBatches.contains(batchname))){
							addBatchAdapterDependency(batchname, adaptername);
						
						}
					}
				}
			}
		}

	
		/*BatchDefinitionBean bean=getBatchDefniton(batchName);
		String dependentAdapters[]= bean.getdependentAdapters();
		for(int i=0;i<dependentAdapters.length;i++){
		removeBatchfrmAdapterKey(dependentAdapters[i], batchName);
		}*/
		
	
}
