/**
 * 
 */
package com.mcg.batch.core;

import static com.mcg.batch.core.BatchWiringConstants.BATCH_META_STORE_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SINGLETON;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_REGISTRY_COMPONENT;
import static com.mcg.batch.core.ContextFactory.getInstance;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.DefaultJobLoader;
import org.springframework.batch.core.configuration.support.JobLoader;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.mcg.batch.adapter.AdapterDefinitionBean;
import com.mcg.batch.core.support.BatchDefinitionBean;
import com.mcg.batch.store.BatchMetadataStore;

/**
 * This class is an implementation of {@link JobRegistry}. The implementation is
 * similar to that of the {@link MapJobRegistry} however there is a check with
 * the {@link BatchCache} on every operation to keep it uptodate.This is
 * required as in a clustered environment {@link BatchCache} is the source of
 * truth for any metadata.
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 * @see {@link JobRegistry} , {@link MapJobRegistry}
 */
@Component(SMART_BATCH_REGISTRY_COMPONENT)
@Scope(SINGLETON)
public class SmartBatchRegistry implements JobRegistry {

	private final ConcurrentMap<String, JobFactory> map = new ConcurrentHashMap<String, JobFactory>();
	private final ConcurrentHashMap<String, Integer> jobVerions = new ConcurrentHashMap<String, Integer>();

	private boolean initComplete = false;

	@Autowired(required = true)
	@Qualifier(BATCH_META_STORE_COMPONENT)
	private BatchMetadataStore batchMetaDataStore;
	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchRegistry.class);

	/**
	 * prevent external instantiation
	 */
	private SmartBatchRegistry() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.configuration.JobLocator#getJob(java.lang
	 * .String)
	 */
	@Override
	public Job getJob(String batchName) throws NoSuchJobException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("SmartBatchRegistry.getJob() started for batch name "
					+ batchName + " at instance " + this);
		}
		JobFactory factory = null;
		try {

			try {
				int batchStoreVersion = batchMetaDataStore
						.getBatchVersion(batchName);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Batch Store Version is :" + batchStoreVersion);
					LOGGER.debug("Versions Map" + jobVerions);
				}
				if (batchStoreVersion != -1) {

					if (!jobVerions.containsKey(batchName) || !map.containsKey(batchName)) {
						try {
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("Loading Batch as its not versioned in local cache...");
							}

							loadBatch(batchName);
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("Batch Store Version is :"
										+ batchStoreVersion);
								LOGGER.debug("Versions Map" + jobVerions);
							}
						} catch (DuplicateJobException e) {
							/**
							 * This exception is not expected as it is called
							 * firstTime
							 */
							LOGGER.error("Duplicate Job Found", e);
						}
					} else if (batchStoreVersion != jobVerions.get(batchName)) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Loading Batch as the version in store and the localcache is different...");
						}
						map.remove(batchName);
						try {
							loadBatch(batchName);

						} catch (DuplicateJobException e) {
							/**
							 * This exception is not expected as the map.remove
							 * is called before loadBatch
							 */
							LOGGER.error("Duplicate Job Found", e);
						}

					}
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("The map is " + map);
					}
					factory = (JobFactory) this.map.get(batchName);

				}
			} catch (Exception ex) {
				map.remove(batchName);
				jobVerions.remove(batchName);
				LOGGER.error("exception while creating a job", ex);
				throw new RuntimeException("Unable to create the batch Job", ex);
			}

			if (factory == null) {
				map.remove(batchName);
				jobVerions.remove(batchName);
				throw new NoSuchJobException(
						"No job configuration with the name [" + batchName
								+ "] was registered");
			}
			return factory.createJob();
		} finally {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("SmartBatchRegistry.getJob() completed");
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.configuration.ListableJobLocator#getJobNames
	 * ()
	 */
	@Override
	public Collection<String> getJobNames() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("SmartBatchRegistry.getJobNames() started ");
		}
		try {
			return batchMetaDataStore.getReisteredBatchNames();

		} finally {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("SmartBatchRegistry.getJobNames() completed");
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.configuration.JobRegistry#register(org
	 * .springframework.batch.core.configuration.JobFactory)
	 */
	@Override
	public void register(JobFactory jobFactory) throws DuplicateJobException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("SmartBatchRegistry.register() started " + jobFactory);
		}
		try {

			Assert.notNull(jobFactory);
			String name = jobFactory.getJobName();
			Assert.notNull(name, "Job configuration must have a name.");
			this.map.putIfAbsent(name, jobFactory);
			// if (previousValue != null) {
			// throw new DuplicateJobException(
			// "A job configuration with this name [" + name
			// + "] was already registered");
			// }

		} finally {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("SmartBatchRegistry.register() completed and the map is "
						+ map);
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.core.configuration.JobRegistry#unregister(java
	 * .lang.String)
	 */
	@Override
	public void unregister(String name) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("SmartBatchRegistry.unregister() started ");
		}
		try {

			Assert.notNull(name, "Job configuration must have a name.");
			this.map.remove(name);
		} finally {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("SmartBatchRegistry.unregister() completed");
			}

		}
	}

	/**
	 * Int method to load a job.
	 */
	@Deprecated
	public void init() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("SmartBatchRegistry.init() started ");
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Init Method called");
		}
		if (!initComplete) {
			map.clear();
			jobVerions.clear();
			Set<String> keys = batchMetaDataStore.getReisteredBatchNames();

			try {
				for (String key : keys) {

					try {
						loadBatch(key);
					} catch (DuplicateJobException exception) {
						/**
						 * Since the values are extracted from a Set<String> and
						 * no entries is present in the map, duplicate jobs are
						 * not expected. Hence it will just log and continue
						 */

						LOGGER.error(
								"Duplicate Job encountered for key " + key,
								exception);
					}
					key = null;
				}

				this.initComplete = true;
			} finally {
				keys = null;

			}

		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("SmartBatchRegistry.init() completed");
		}

	}

	private synchronized void loadBatch(String batch)
			throws DuplicateJobException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRegistry.loadBatch() started ");
		}
		BatchDefinitionBean batchDefnBean = null;
		String[] dependentAdapters = null;
		AdapterDefinitionBean adapterDefnBean;
		DefaultListableBeanFactory beanFactory;
		GenericXmlApplicationContext context = null;
		String[] beanNames = null;
		BeanDefinition beanDefinition;

		try {
			batchDefnBean = batchMetaDataStore.getBatchDefniton(batch);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("the batch defn Bean is " + batchDefnBean);
			}
			this.jobVerions.put(batch, batchDefnBean.getVersion());

			dependentAdapters = batchDefnBean.getdependentAdapters();
			if (dependentAdapters != null && dependentAdapters.length > 0) {
				beanFactory = ContextFactory.getInstance().getBeanFactory();
				for (String dependentAdapter : dependentAdapters) {
					adapterDefnBean = batchMetaDataStore
							.getAdapter(dependentAdapter);
					context = new GenericXmlApplicationContext(
							new ByteArrayResource(adapterDefnBean
									.getAdapterDefinition().getBytes()));
					beanNames = context.getDefaultListableBeanFactory()
							.getBeanDefinitionNames();
					for (String beanName : beanNames) {
						beanDefinition = context
								.getDefaultListableBeanFactory()
								.getBeanDefinition(beanName);
					
							beanFactory.registerBeanDefinition(beanName,
									beanDefinition);
							LOGGER.info("Register bean definiton for bean"
									+ beanName);
			
						beanName = null;
					}
					if (context != null) {
						context.close();
					}
					context = null;
					beanNames = null;
				}
			}
			loadBatchToRegistry(this, batchDefnBean.getBatchXmlAsBytes());
		} finally {
			if (context != null) {
				context.close();
			}
			beanFactory = null;
			adapterDefnBean = null;
			batchDefnBean = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRegistry.loadBatch() completed");
			}
		}
	}

	private static final void loadBatchToRegistry(JobRegistry registry,
			byte[] batchDefinition) throws DuplicateJobException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRegistry.loadBatchToRegistry() started ");
		}
		JobLoader loader = null;
		try {
			loader = new DefaultJobLoader(registry);
			loader.load(getInstance()
					.aquireBatchContextFactory(batchDefinition));
		} finally {
			loader = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRegistry.loadBatchToRegistry() completed");
			}

		}
	}

}
