/**
 * 
 */
package com.mcg.batch.cache.impl;

import com.mcg.batch.cache.SmartBatchCache;
import com.mcg.batch.exceptions.CacheException;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public abstract class AbstractCache implements SmartBatchCache {

	protected String nameSpace;

	/**
	 * @param nameSpace
	 */
	protected AbstractCache(String nameSpace) throws CacheException {
		this.nameSpace = nameSpace;
		init();
	}

	/**
	 * @return the nameSpace
	 */
	@Override
	public String getCurrentNameSpace() {
		return nameSpace;
	}

}
