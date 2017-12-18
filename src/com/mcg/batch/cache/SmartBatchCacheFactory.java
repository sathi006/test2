/**
 * 
 */
package com.mcg.batch.cache;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public interface SmartBatchCacheFactory {
	/**
	 * Provides the SmartBatchCache based on the nameSpace provided
	 * 
	 * @param nameSpace
	 * @return
	 */
	public SmartBatchCache getCacheFor(String nameSpace);

	/**
	 * Provides the SmartBatchCache which is considered as default
	 * 
	 * @param nameSpace
	 * @return
	 */
	public SmartBatchCache getDefault();

	/**
	 * Provide the SmartBatch Cache based on thread context. <br>
	 * Returns Default if no thread context is found
	 * 
	 * @return
	 */
	public SmartBatchCache getCache();

	/**
	 * 
	 * A Method to register the SmartBatchCache against the nameSpace
	 * 
	 * @param nameSpace
	 * @param smartBatchCache
	 */

	public void register(String nameSpace, SmartBatchCache smartBatchCache);

	/**
	 * A Method to update the SmartBatchCache specified by the namespace
	 * 
	 * @param nameSpace
	 * @param smartBatchCache
	 */

	public void update(String nameSpace, SmartBatchCache smartBatchCache);

	/**
	 * A method to unregister the SmartBatchCache specified by the namespace
	 * 
	 * @param nameSpace
	 */

	public void unRegister(String nameSpace);

}
