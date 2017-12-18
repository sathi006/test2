/**
 * 
 */
package com.mcg.batch.store;

import java.util.HashMap;
import java.util.Set;

import org.springframework.batch.core.configuration.JobRegistry;

import com.mcg.batch.adapter.AdapterDefinitionBean;
import com.mcg.batch.core.support.BatchDefinitionBean;

// TODO: Auto-generated Javadoc
/**
 * The Interface BatchMetadataStore.
 *
 * @author Nanda Gopalan
 * @version 1.0
 * @since:1.0 
 */
public interface BatchMetadataStore {

	/**
	 * Provides access to all register BatchDefiniton This is typically used by
	 * the {@link JobRegistry}.
	 *
	 * @return the reistered batch names
	 * @see {@link JobRegistry}
	 */
	public Set<String> getReisteredBatchNames();

	/**
	 * Provides access to all register BatchDefiniton This is typically used by
	 * the {@link JobRegistry}.
	 *
	 * @return the all batch names
	 * @see {@link JobRegistry}
	 */
	public Set<String> getAllBatchNames();

	/**
	 * Provides access to all registerd BatchDefiniton . This would return the
	 * batch names even if it was deleted.
	 *
	 * @return the total batches registered
	 * @see {@link JobRegistry}
	 */
	public Set<String> getTotalBatchesRegistered();

	/**
	 * Register a new BatchDefinition Bean.
	 *
	 * @param bean the bean
	 * @See {@link BatchDefinitionBean}
	 */
	public void registerBatch(BatchDefinitionBean bean);

	/**
	 * Re-Register a BatchDefinition that was unregistered before.<br>
	 * Calling this method for a batch name that is already registered will have
	 * no effect.
	 *
	 * @param batchName the batch name
	 */
	public void reRegisterBatch(String batchName);

	/**
	 * Unregister a Batch and optionally remove if required.
	 *
	 * @param batchName the batch name
	 * @param remove the remove
	 */
	public void unRegisterBatch(String batchName, boolean remove);

	/**
	 * Get the BatchDefinition based on the batchName The batch name should be
	 * unique and it is expected that the application creating that would
	 * maintain the uniqueness accross domain names.
	 *
	 * @param name the name
	 * @return the batch defniton
	 * @See {@link BatchDefinitionBean}
	 */
	public BatchDefinitionBean getBatchDefniton(String name);

	/**
	 * This method gives the latest version of a Batch that is registered This
	 * is typically used by the {@link JobRegistry}.
	 *
	 * @param batchName the batch name
	 * @return the batch version
	 * @see {@link JobRegistry}
	 */

	public int getBatchVersion(String batchName);

	/**
	 * Provides a boolan flag indicating if the batch job is registered.
	 *
	 * @param batchName the batch name
	 * @return true, if is batch reistered
	 */
	public boolean isBatchReistered(String batchName);

	/**
	 * Adds the properties.
	 *
	 * @param property the property
	 * @param key the key
	 * @param value the value
	 */
	public void addProperties(String property, String key, String value);

	/**
	 * Get a property identified by propertyName and a key.
	 *
	 * @param property the property
	 * @param key the key
	 * @return the property
	 */
	public String getProperty(String property, String key);

	/**
	 * Remove the property key specified.
	 *
	 * @param property the property
	 * @param key the key
	 */
	public void removePropertyKey(String property, String key);

	/**
	 * Remove all the property keys.
	 *
	 * @param property the property
	 */
	public void removeAllPropertyKeys(String property);

	/**
	 * Provide the list of all property keys.
	 *
	 * @param property the property
	 * @return the property keys
	 */
	public Set<String> getPropertyKeys(String property);

	/**
	 * Provide the list of all property registered with store.
	 *
	 * @return the all property names
	 */
	public Set<String> getAllPropertyNames();

	/**
	 * Get property key and values from the store.
	 *
	 * @param propertyName the property name
	 * @return the property details
	 */
	public HashMap<String, String> getPropertyDetails(String propertyName);

	/**
	 * Register an adapter Definition Bean.
	 *
	 * @param adapterDefnbean the adapter defnbean
	 */
	public void registerAdapter(AdapterDefinitionBean adapterDefnbean);

	/**
	 * Returns the adapter definition bean from the backend store.
	 * 
	 * @param adapterName
	 * @return
	 */
	public AdapterDefinitionBean getAdapter(String adapterName);

	/**
	 * Removes the adapter definition bean from the backend store.
	 * 
	 * @param adapterName
	 * @return
	 */
	public void removeAdapter(String adapterName);

	/**
	 * Returns all adapter names from the backend store.
	 * 
	 * @param adapterName
	 * @return
	 */
	public Set<String> getAllAdapterNames();

	/**
	 * Get All the dependent Beans per batch.
	 *
	 * @param batchName the batch name
	 * @return the all dependednts
	 */
	public Set<String> getAllDependednts(String batchName);

	/**
	 * Set a dependency for an batch.
	 *
	 * @param adapterName the adapter name
	 * @param batchName the batch name
	 */
	public void addAdapterBatchDependency(String adapterName, String batchName);
	
	/**
	 * Move entries to cache.
	 *
	 * @param mapName the map name
	 */
	public void moveEntriesToCache(String mapName);

	
	/**
	 * Move all adapters to domain based cache.
	 */
	public void moveAllAdaptersToDomainBasedCache();
	
	public Set<String> getAllBatchesfrmAdapter(String adapter);
	
	public void addBatchAdapterDependency(String batchName,String adapterName);
	
	public void removeBatchfrmAdapterKey(String adapterName, String batchvalue);
	
	public void loadAllBatchesdepenAdapters();



}
