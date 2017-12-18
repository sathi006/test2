/**
 * 
 */
package com.mcg.batch.cache.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConverters;
import org.springframework.util.Assert;

import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class JedisConenctionFactory implements RedisConnectionFactory {

	private Pool<Jedis> jedisPool;

	private int dbIndex = 0;
	private boolean convertPipelineAndTxResults = true;

	/**
	 * @param jedisPool
	 */
	public JedisConenctionFactory(Pool<Jedis> jedisPool) {
		super();
		this.jedisPool = jedisPool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.redis.connection.RedisConnectionFactory#
	 * getConnection()
	 */
	@Override
	public RedisConnection getConnection() {
		return new JedisConnection(jedisPool.getResource(), jedisPool, dbIndex);
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see org.springframework.dao.support.PersistenceExceptionTranslator#
	// * translateExceptionIfPossible(java.lang.RuntimeException)
	// */
	// @Override
	// public DataAccessException translateExceptionIfPossible(RuntimeException
	// ex) {
	// return JedisUtils.convertJedisAccessException(ex);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.dao.support.PersistenceExceptionTranslator#
	 * translateExceptionIfPossible(java.lang.RuntimeException)
	 */
	@Override
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		return JedisConverters.toDataAccessException(ex);
	}

	/**
	 * @return the dbIndex int
	 */
	public int getDbIndex() {
		return dbIndex;
	}

	/**
	 * @param dbIndex
	 *            int
	 */
	public void setDbIndex(int dbIndex) {
		Assert.isTrue(dbIndex >= 0,
				"invalid DB index (a positive index required)");
		this.dbIndex = dbIndex;
	}

	/**
	 * @param convertPipelineAndTxResults
	 *            boolean
	 */
	public void setConvertPipelineAndTxResults(
			boolean convertPipelineAndTxResults) {
		this.convertPipelineAndTxResults = convertPipelineAndTxResults;
	}

	/**
	 * @return the convertPipelineAndTxResults boolean
	 */
	@Override
	public boolean getConvertPipelineAndTxResults() {
		return convertPipelineAndTxResults;
	}

}
