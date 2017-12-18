/**
 */
package com.mcg.batch.cache.impl;

import static com.mcg.batch.core.BatchConfiguration.getProperty;
import static com.mcg.batch.utils.StringHelper.HYPHEN_CHAR;
import static com.mcg.batch.utils.StringHelper.NEW_LINE;
import static com.mcg.batch.utils.StringHelper.concat;
import static com.mcg.batch.utils.StringHelper.isNotEmpty;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import com.mcg.batch.core.BatchConfiguration;
import com.mcg.batch.exceptions.CacheException;
import com.mcg.batch.utils.StringHelper;
import com.mcg.batch.utils.ThreadContextUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;


/**
 * The Class RedisCache.
 *
 * @author Nanda Gopalan
 * @version 1.0
 * @since:1.0 
 */

public class RedisCache extends AbstractCache {

	/** The Constant CACHE_CONFIG_FILES. */
	private static final ConcurrentMap<String, String> CACHE_CONFIG_FILES = new ConcurrentHashMap<String, String>();

	/** The Constant REDIS_CACHE_PROPERTY_PREFIX. */
	private static final String REDIS_CACHE_PROPERTY_PREFIX = "redis.cache.file.";
	
	/** The Constant REDIS_MASTER_HOST. */
	private static final String REDIS_MASTER_HOST = "cache.master.host";
	
	/** The Constant REDIS_MASTER_PORT. */
	private static final String REDIS_MASTER_PORT = "cache.master.port";
	
	/** The Constant REDIS_POOL_MAX_TOTAL. */
	private static final String REDIS_POOL_MAX_TOTAL = "cache.pool.maxTotal";
	
	/** The Constant REDIS_POOL_MAX_IDLE. */
	private static final String REDIS_POOL_MAX_IDLE = "cache.pool.maxIdle";
	
	/** The Constant REDIS_POOL_MIN_IDLE. */
	private static final String REDIS_POOL_MIN_IDLE = "cache.pool.minIdle";
	
	/** The Constant REDIS_POOL_MAX_WAIT. */
	private static final String REDIS_POOL_MAX_WAIT = "cache.pool.maxWait";
	
	/** The Constant REDIS_POOL_MIN_EVICTABLE_IDLE_TIME. */
	private static final String REDIS_POOL_MIN_EVICTABLE_IDLE_TIME = "cache.pool.minEvictableIdleTime";
	
	/** The Constant REDIS_POOL_NUM_TESTS_PER_EVICTIONS. */
	private static final String REDIS_POOL_NUM_TESTS_PER_EVICTIONS = "cache.pool.numTestPerEvictions";
	
	/** The Constant REDIS_AUTHENTICATION_ENABLED. */
	private static final String REDIS_AUTHENTICATION_ENABLED = "cache.auth.enabled";
	
	/** The Constant REDIS_AUTH_CREDENTIALS. */
	private static final String REDIS_AUTH_CREDENTIALS = "cache.auth.credentials";
	
	/** The Constant REDIS_SENTINELS_LIST. */
	private static final String REDIS_SENTINELS_LIST = "cache.sentinels.list";
	
	/** The Constant REDIS_CACHE_MASTER_NAME. */
	private static final String REDIS_CACHE_MASTER_NAME = "cache.master.name";
	
	/** The Constant REDIS_CACHE_DEFAULT_TIMEOUT. */
	private static final String REDIS_CACHE_DEFAULT_TIMEOUT = "cache.default.timeout";

	// private static final String REDIS_AUTHENTICATION_PRINCIPAL =
	// "cache.auth.principal";
	// "cache.auth.credential";
	/** Logger to be used by this class. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RedisCache.class);

	/** The redis template. */
	private RedisTemplate<Serializable, Serializable> redisTemplate;

	/**
	 * Instantiates a new redis cache.
	 *
	 * @param nameSpace the name space
	 * @throws CacheException the cache exception
	 */
	public RedisCache(final String nameSpace) throws CacheException {
		super(nameSpace);
	}

	/**
	 * Apply masterconfig change.
	 *
	 * @param host the host
	 * @param port the port
	 */
	public static void applyMasterconfigChange(String host, String port) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCache#init()
	 */
	@Override
	public void init() throws CacheException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.init() started ");
		}
		Properties prop = new Properties();
		FileInputStream filein = null;
		String cacheHost = null;
		String authCredentials = null;
		String sentinels = null;
		String cacheMasterName = null;
		boolean authEnabled = false;
		int cachePort = -1;
		int minIdle;
		int maxIdle;
		int maxTotal;
		int numTestPerEvictions;
		int defaultCacheTimeout;
		long maxWait;
		long minEvictableIdle;

		try {
			if (redisTemplate == null) {
				redisTemplate = new RedisTemplate<Serializable, Serializable>();
			}
			filein = new FileInputStream(getProperty(concat(
					REDIS_CACHE_PROPERTY_PREFIX, nameSpace)));
			prop.load(filein);

			cacheHost = prop.getProperty(REDIS_MASTER_HOST, "localhost");
			cachePort = Integer.parseInt(prop.getProperty(REDIS_MASTER_PORT,
					"6379"));
			sentinels = prop.getProperty(REDIS_SENTINELS_LIST);
			cacheMasterName = prop.getProperty(REDIS_CACHE_MASTER_NAME);
			minIdle = Integer.parseInt(prop.getProperty(REDIS_POOL_MIN_IDLE,
					"10"));
			maxIdle = Integer.parseInt(prop.getProperty(REDIS_POOL_MAX_IDLE,
					"10"));
			CACHE_CONFIG_FILES.put(concat(cacheHost, cachePort), nameSpace);
			maxTotal = Integer.parseInt(prop.getProperty(REDIS_POOL_MAX_TOTAL,
					"100"));
			numTestPerEvictions = Integer.parseInt(prop.getProperty(
					REDIS_POOL_NUM_TESTS_PER_EVICTIONS, "3"));
			defaultCacheTimeout = Integer.parseInt(prop.getProperty(
					REDIS_CACHE_DEFAULT_TIMEOUT, "0"));
			maxWait = Long.parseLong(prop.getProperty(REDIS_POOL_MAX_WAIT,
					"5000"));
			minEvictableIdle = Long.parseLong(prop.getProperty(
					REDIS_POOL_MIN_EVICTABLE_IDLE_TIME, "300000"));

			JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMinIdle(minIdle);
			poolConfig.setMaxIdle(maxIdle);
			poolConfig.setMaxTotal(maxTotal);
			poolConfig.setMaxWaitMillis(maxWait);
			poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdle);
			poolConfig.setNumTestsPerEvictionRun(numTestPerEvictions);
			authEnabled = Boolean.valueOf(prop
					.getProperty(REDIS_AUTHENTICATION_ENABLED));
			Pool<Jedis> jedisPool = null;
			if (authEnabled) {
				authCredentials = prop.getProperty(REDIS_AUTH_CREDENTIALS);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Cache sentinelsList " + sentinels);
				LOGGER.debug("Cache cacheMasterName " + cacheMasterName);
				LOGGER.debug("Cache host name " + cacheHost);
				LOGGER.debug("Cache port " + cachePort);
				LOGGER.debug("Cache is AuthEnabled " + authEnabled);
				LOGGER.debug("Cache pool minIdle " + minIdle);
				LOGGER.debug("Cache pool maxIdle " + maxIdle);
				LOGGER.debug("Cache pool maxTotal " + maxTotal);
				LOGGER.debug("Cache pool maxWait " + maxWait);
				LOGGER.debug("Cache pool numTestPerEvictions "
						+ numTestPerEvictions);
				LOGGER.debug("Cache pool minEvictableIdle " + minEvictableIdle);
				LOGGER.debug("Cache authEnabled " + authEnabled);
				LOGGER.debug("Cache authCredentials " + authCredentials);
			}
			if (isNotEmpty(sentinels) && isNotEmpty(cacheMasterName)) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Using Sentinels as it is configured");
				}
				Set<String> sentinelSet = new HashSet<String>(
						Arrays.asList(sentinels.split(StringHelper.COMMA)));
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The Sentines to be used is " + sentinels);
				}

				if (authEnabled && isNotEmpty(authCredentials)) {
					jedisPool = new JedisSentinelPool(cacheMasterName,
							sentinelSet, poolConfig,defaultCacheTimeout,authCredentials);

				} else {
					jedisPool = new JedisSentinelPool(cacheMasterName,
							sentinelSet, poolConfig,defaultCacheTimeout);
				}

			} else {
				if (authEnabled && isNotEmpty(authCredentials)) {
					jedisPool = new JedisPool(poolConfig, cacheHost, cachePort,
							defaultCacheTimeout, authCredentials);

				} else {
					jedisPool = new JedisPool(poolConfig, cacheHost, cachePort,
							defaultCacheTimeout);
				}
			}
			JedisConenctionFactory conenctionFactory = new JedisConenctionFactory(
					jedisPool);
			redisTemplate.setConnectionFactory(conenctionFactory);
			redisTemplate
					.setKeySerializer(new JdkSerializationRedisSerializer());
			redisTemplate
					.setValueSerializer(new JdkSerializationRedisSerializer());
			redisTemplate
					.setHashKeySerializer(new JdkSerializationRedisSerializer());
			redisTemplate
					.setHashValueSerializer(new JdkSerializationRedisSerializer());
			redisTemplate.afterPropertiesSet();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("redisTemplate init completed successfully");
			}

			// TODO implement the authentication framework if required.
		} catch (IOException e) {
			LOGGER.error(
					"Failed to load the properties for the redis cache server details..",
					e);
			throw new CacheException(
					"Failed to load the properties for the redis cache server details...",
					e);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.init() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCache#putToMap(java.lang.String,
	 * java.io.Serializable, java.io.Serializable)
	 */

	@Override
	public void putToMap(final String mapName, final Serializable key,
			final Serializable value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.putToMap() started");
		}
		try {
			redisTemplate.opsForHash().put(getKey(mapName), key, value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.putToMap() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#putIfAbsentToMap(java.lang.String
	 * , java.io.Serializable, java.io.Serializable)
	 */
	@Override
	public boolean putIfAbsentToMap(final String mapName,
			final Serializable key, final Serializable value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.putIfAbsentToMap() started");
		}
		try {
			return redisTemplate.opsForHash().putIfAbsent(getKey(mapName), key,
					value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.putIfAbsentToMap() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#removeFromMap(java.lang.String,
	 * java.io.Serializable)
	 */
	@Override
	public void removeFromMap(final String mapName, final Serializable key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.removeFromMap() started");
		}
		try {
			redisTemplate.opsForHash().delete(getKey(mapName), key);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.removeFromMap() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#getfromMap(java.lang.String,
	 * java.io.Serializable)
	 */
	@Override
	public Serializable getFromMap(final String mapName, final Serializable key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getfromMap() started");
		}
		try {
			try {
				return (Serializable) redisTemplate.opsForHash().get(
						getKey(mapName), key);
			} catch (NullPointerException npe) {
				return null;
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getfromMap() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#getFromMap(java.lang.String,
	 * java.io.Serializable, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T getFromMap(final String mapName,
			final Serializable key, Class<T> clazz) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getfromMap() started");
		}
		try {
			return (T) redisTemplate.opsForHash().get(getKey(mapName), key);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getfromMap() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#hasKeyInMap(java.lang.String,
	 * java.io.Serializable)
	 */
	@Override
	public boolean hasKeyInMap(String mapName, Serializable key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.hasKeyInMap() started");
		}
		try {
			return redisTemplate.opsForHash().hasKey(getKey(mapName), key);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.hasKeyInMap() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#getAllKeys(java.lang.String)
	 */
	@Override
	public Set<Object> getAllKeys(final String mapName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getfromMap() started");
		}
		try {
			return redisTemplate.opsForHash().keys(getKey(mapName));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getfromMap() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#appendTolist(java.lang.String,
	 * java.io.Serializable)
	 */
	@Override
	public long appendTolist(final String listName, final Serializable value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.appendTolist() started");
		}
		try {
			return redisTemplate.opsForList()
					.rightPush(getKey(listName), value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.appendTolist() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#appendTolistNoNs(java.lang.String
	 * , java.io.Serializable)
	 */
	@Override
	public long appendTolistNoNs(final String listName, final Serializable value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.appendTolistNoNs() started");
		}
		try {
			return redisTemplate.opsForList().rightPush(listName, value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.appendTolistNoNs() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCache#addToList(java.lang.String,
	 * java.io.Serializable, long)
	 */
	@Override
	public void addToList(String listName, Serializable value, long index) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.addToList() started");
		}
		try {
			redisTemplate.opsForList().set(getKey(listName), index, value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.addToList() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#addToListNoNs(java.lang.String,
	 * java.io.Serializable, long)
	 */
	@Override
	public void addToListNoNs(String listName, Serializable value, long index) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.addToListNoNs() started");
		}
		try {
			redisTemplate.opsForList().set(listName, index, value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.addToListNoNs() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCache#getList(java.lang.String,
	 * long, long)
	 */
	@Override
	public List<Serializable> getList(String listName, long start, long end) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getList() started");
		}
		try {
			return redisTemplate.opsForList().range(getKey(listName), start,
					end);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getList() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCache#getListNoNs(java.lang.String,
	 * long, long)
	 */ 
	@Override
	public List<Serializable> getListNoNs(String listName, long start, long end) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getListNoNs() started");
		}
		try {
			return redisTemplate.opsForList().range(listName, start,
					end);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getListNoNs() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCache#getList(java.lang.String)
	 */
	@Override
	public List<Serializable> getList(String listName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getList() started");
		}
		try {
			return redisTemplate.opsForList().range(getKey(listName), 0,
					redisTemplate.opsForList().size(getKey(listName)));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getList() completed");
			}
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCache#getListNoNs(java.lang.String)
	 */
	@Override
	public List<Serializable> getListNoNs(String listName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getListNoNs() started");
		}
		try {
			return redisTemplate.opsForList().range(listName, 0,
					redisTemplate.opsForList().size(listName));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getListNoNs() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#getFromList(java.lang.String,
	 * long)
	 */
	@Override
	public Serializable getFromList(String listName, long index) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getFromList() started");
		}
		try {
			return redisTemplate.opsForList().index(getKey(listName), index);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getFromList() completed");
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#getFromListNoNs(java.lang.String,
	 * long)
	 */
	@Override
	public Serializable getFromListNoNs(String listName, long index) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getFromListNoNs() started");
		}
		try {
			return redisTemplate.opsForList().index(listName, index);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getFromListNoNs() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#removeFromList(java.lang.String,
	 * long)
	 */
	@Override
	public void removeFromList(String listName, long count, Serializable value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.removeFromList() started");
		}
		try {
			redisTemplate.opsForList().remove(getKey(listName), count, value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.removeFromList() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#removeFromListNoNs(java.lang.String,
	 * long)
	 */
	@Override
	public void removeFromListNoNs(String listName, long count, Serializable value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.removeFromListNoNs() started");
		}
		try {
			redisTemplate.opsForList().remove(listName, count, value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.removeFromListNoNs() completed");
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCache#add(java.io.Serializable,
	 * java.io.Serializable)
	 */
	@Override
	public void add(Serializable key, Serializable value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.add() started");
		}
		try {
			redisTemplate.opsForValue().set(getKey(key), value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.add() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#remove(java.io.Serializable)
	 */
	@Override
	public void remove(Serializable key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.remove() started");
		}
		try {
			redisTemplate.delete(getKey(key));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.remove() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#putIfAbsent(java.io.Serializable
	 * , java.io.Serializable)
	 */
	@Override
	public void putIfAbsent(Serializable key, Serializable value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.putIfAbsent() started");
		}
		try {
			redisTemplate.opsForValue().setIfAbsent(getKey(key), value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.putIfAbsent() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCache#get(java.io.Serializable)
	 */
	@Override
	public Serializable get(Serializable key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.get() started");
		}
		try {
			return redisTemplate.opsForValue().get(getKey(key));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.get() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#getAndSet(java.io.Serializable,
	 * java.io.Serializable)
	 */
	@Override
	public Serializable getAndSet(Serializable key, Serializable value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getAndSet() started");
		}
		try {
			return redisTemplate.opsForValue().getAndSet(getKey(key), value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getAndSet() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#incrementAndGet(java.lang.String
	 * )
	 */
	@Override
	public long incrementAndGet(Serializable key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.incrementAndGet() started");
		}
		try {
			if (BatchConfiguration.USE_DOMAIN_SEQUENCE) {
				return redisTemplate.opsForValue().increment(getKey(key), 1);
			} else {
				return redisTemplate.opsForValue().increment(key, 1);
			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.incrementAndGet() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#incrementAndGet(java.io.Serializable
	 * , int)
	 */
	@Override
	public long incrementAndGet(Serializable key, int delta) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.incrementAndGet() started");
		}
		try {
			if (BatchConfiguration.USE_DOMAIN_SEQUENCE) {
				return redisTemplate.opsForValue()
						.increment(getKey(key), delta);
			} else {
				return redisTemplate.opsForValue().increment(key, delta);
			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.incrementAndGet() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#hasKey(java.io.Serializable)
	 */
	@Override
	public boolean hasKey(Serializable key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.hasKey() started");
		}
		try {
			return redisTemplate.opsForValue().get(getKey(key)) != null;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.hasKey() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.cache.SmartBatchCache#addAndGet(java.io.Serializable)
	 */
	@Override
	public long addAndGet(Serializable key, long delta) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.addAndGet() started");
		}
		try {
			if (BatchConfiguration.USE_DOMAIN_SEQUENCE) {
				return redisTemplate.opsForValue()
						.increment(getKey(key), delta);
			} else {
				return redisTemplate.opsForValue().increment(key, delta);
			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.addAndGet() completed");
			}
		}
	}

	/**
	 * Gets the key.
	 *
	 * @param originalKey the original key
	 * @return the key
	 */
	private static Serializable getKey(Serializable originalKey) {

		if (BatchConfiguration.DOMAIN_SPECIFIC_STORE) {
			return originalKey;
		} else {
			return concat(ThreadContextUtils.getNamespace(), HYPHEN_CHAR,
					originalKey);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mcg.batch.cache.SmartBatchCache#appendToMapEntry(java.lang.String, java.io.Serializable, java.lang.String, java.lang.String)
	 */
	public void appendToMapEntry(String mapName, Serializable key,String value){
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.appendToMapEntry() started");
		}
		try {
		    String existingValue = (String) getFromMap(mapName, key);
                    if(!StringHelper.isEmpty(existingValue)) {
                	   value =  StringHelper.concatNotNulls(existingValue, NEW_LINE, value);
                    }
                    if (!StringHelper.isEmpty(value)) {
                    putToMap(mapName, key, value);
                    }
		  }
		 finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.appendToMapEntry() completed");
			}
		}
		
	}
	
	public Serializable getFromMapNoNS(final String mapName, final Serializable key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getFromMapNoNS() started");
		}
		try {
			try {
				return (Serializable) redisTemplate.opsForHash().get(
						mapName, key);
			} catch (NullPointerException npe) {
				return null;
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getFromMapNoNS() completed");
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.cache.SmartBatchCache#putToMap(java.lang.String,
	 * java.io.Serializable, java.io.Serializable)
	 */

	
	public void putToMapNoNS(final String mapName, final Serializable key,
			final Serializable value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.putToMapNoNS() started");
		}
		try {
			redisTemplate.opsForHash().put(mapName, key, value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.putToMapNoNS() completed");
			}
		}
	}

	
	public void removeFromMapNoNS(final String mapName, final Serializable key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.removeFromMapNoNS() started");
		}
		try {
			redisTemplate.opsForHash().delete(mapName, key);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.removeFromMapNoNS() completed");
			}
		}
	}
	
	@Override
	public Set<Object> getAllKeysNoNS(final String mapName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisCache.getAllKeysNoNS() started");
		}
		try {
			return redisTemplate.opsForHash().keys(mapName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisCache.getAllKeysNoNS() completed");
			}
		}
	}
	
	
	
	
}
