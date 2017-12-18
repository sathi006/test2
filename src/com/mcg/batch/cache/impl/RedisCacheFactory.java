/**
 * 
 */
package com.mcg.batch.cache.impl;

import static com.mcg.batch.core.BatchConfiguration.DOMAIN_SPECIFIC_STORE;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcg.batch.cache.SmartBatchCache;
import com.mcg.batch.cache.SmartBatchCacheFactory;
import com.mcg.batch.exceptions.CacheException;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class RedisCacheFactory implements SmartBatchCacheFactory {

	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RedisCacheFactory.class);

	/**
	 * In-Memory references to SmartBatchCaches based on namespace
	 */
	private static final ConcurrentHashMap<String, SmartBatchCache> REGISTERED_CACHES = new ConcurrentHashMap<String, SmartBatchCache>();

	/**
	 * 
	 */
	public static final String DEFAULT_CACHE_NAME = "default";

	/**
	 * prevent external instantiation
	 */
	private RedisCacheFactory() {
		synchronized (REGISTERED_CACHES) {
			try {
				REGISTERED_CACHES.put(DEFAULT_CACHE_NAME, new RedisCache(
						DEFAULT_CACHE_NAME));
			} catch (CacheException e) {
				LOGGER.error("Exception while initializing the default cache",
						e);
			}
		}

	}

	/**
	 * Internal class for lazy initialization.
	 * 
	 * @version 1.0
	 * @since:1.0
	 * @author Nanda Gopalan
	 *
	 */
	private static final class RedisCacheFactoryInner {
		private static final RedisCacheFactory INSTANCE = new RedisCacheFactory();

	}

	/**
	 * Method for access to the singleton.
	 * 
	 * @return
	 */
	public static final RedisCacheFactory getInstance() {
		return RedisCacheFactoryInner.INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCacheFactory#getCacheFor(java.lang
	 * .String)
	 */
	@Override
	public SmartBatchCache getCacheFor(final String nameSpace) {
		return REGISTERED_CACHES.get(nameSpace);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCacheFactory#getDefault()
	 */
	@Override
	public SmartBatchCache getDefault() {

		return getCacheFor(DEFAULT_CACHE_NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCacheFactory#get()
	 */
	@Override
	public SmartBatchCache getCache() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCacheFactory.get() started");
		}
		String namespace = null;
		SmartBatchCache cache = null;
		try {
			if (DOMAIN_SPECIFIC_STORE
					&& (namespace = ThreadContextUtils.getNamespace()) != null) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("The namespace from the thread context is "
							+ namespace);
				}
				if (!REGISTERED_CACHES.containsKey(namespace)) {
					synchronized (REGISTERED_CACHES) {
						try {
							cache = new RedisCache(namespace);
							register(namespace, cache);
						} catch (CacheException e) {
							LOGGER.error(
									"Unable to Create the Redis Cache instance for namespace"
											+ namespace, e);
							throw new RuntimeException(e);
						}
					}
				}
				cache = getCacheFor(namespace);
			} else {
				cache = getDefault();
			}
			return cache;
		} finally {
			namespace = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCacheFactory.get() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCacheFactory#register(java.lang.String
	 * , com.mcg.batch.cache.SmartBatchCache)
	 */
	@Override
	public void register(final String nameSpace,
			final SmartBatchCache smartBatchCache) {
		REGISTERED_CACHES.putIfAbsent(nameSpace, smartBatchCache);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCacheFactory#update(java.lang.String,
	 * com.mcg.batch.cache.SmartBatchCache)
	 */
	@Override
	public void update(String nameSpace, SmartBatchCache smartBatchCache) {
		REGISTERED_CACHES.put(nameSpace, smartBatchCache);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCacheFactory#unRegister(java.lang.
	 * String)
	 */
	@Override
	public void unRegister(final String nameSpace) {
		REGISTERED_CACHES.remove(nameSpace);
	}

}
