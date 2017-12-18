/**
 * 
 */
package com.mcg.batch.store.impl.jdbc;

import static com.mcg.batch.core.BatchConfiguration.NODE_NAME;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.VARCHAR;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class BatchNodeJobDao extends AbstractJdbcBatchMetadataDao {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchNodeJobDao.class);
	private static final String STARTED = "STARTED";

	private static final String INSERT_BATCH_NODE_JOBS = "INSERT INTO %PREFIX%NODE_JOBS (NODE_ID,JOB_EXECUTION_ID,STATUS,LAST_UPDATE_TIME) VALUES (?,?,?,?)";
	private static final String DELETE_BATCH_NODE_JOBS = "DELETE FROM %PREFIX%NODE_JOBS WHERE NODE_ID=? AND JOB_EXECUTION_ID=? ";
	private static final String SELECT_BATCH_NODE_JOBS_STARTED = "SELECT JOB_EXECUTION_ID FROM %PREFIX%NODE_JOBS WHERE NODE_ID=? AND STATUS='STARTED' ";

	/**
	 * Link the started executionid with the current node
	 * 
	 * @param jobExecutionId
	 */
	public void linkNodeStartedJobs(final Long jobExecutionId) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchNodeJobDao.linkNodeStartedJobs() started");
		}
		/*Date currentTime = null;
		Object[] parameters = null;
		int[] paramTypes = null;*/
		try {
			/*currentTime = new Date(System.currentTimeMillis());

			parameters = new Object[] { NODE_NAME, jobExecutionId, STARTED,
					currentTime };
			paramTypes = new int[] { VARCHAR, NUMERIC, VARCHAR, TIMESTAMP };

			getJdbcTemplate().update(getQuery(INSERT_BATCH_NODE_JOBS),
					parameters, paramTypes);*/
		    	linkStartedJobtoNode(jobExecutionId, NODE_NAME);

		} finally {
			/*currentTime = null;
			parameters = null;
			paramTypes = null;*/
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchNodeJobDao.linkNodeStartedJobs() completed");
			}
		}

	}
	
	public void linkStartedJobtoNode(final Long jobExecutionId, final String nodeName) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchNodeJobDao.linkStartedJobtoNode() started");
		}
		Date currentTime = null;
		Object[] parameters = null;
		int[] paramTypes = null;
		try {
			currentTime = new Date(System.currentTimeMillis());

			parameters = new Object[] { nodeName, jobExecutionId, STARTED,
					currentTime };
			paramTypes = new int[] { VARCHAR, NUMERIC, VARCHAR, TIMESTAMP };

			getJdbcTemplate().update(getQuery(INSERT_BATCH_NODE_JOBS),
					parameters, paramTypes);

		} finally {
			currentTime = null;
			parameters = null;
			paramTypes = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchNodeJobDao.linkStartedJobtoNode() completed");
			}
		}

	}

	/**
	 * Provides all the started jobs by this node.
	 * 
	 * @return
	 */
	public List<String> getNodeStartedJobIds() {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchNodeJobDao.getNodeStartedJobIds() started");
		}
		/*Object[] parameters = null;
		int[] paramTypes = null;
		ResultSetExtractor<List<String>> extractor = null;*/
		try {

/*			parameters = new Object[] { NODE_NAME };
			paramTypes = new int[] { VARCHAR };
			extractor = new ResultSetExtractor<List<String>>() {
				
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.springframework.jdbc.core.ResultSetExtractor#extractData(
				 * java.sql.ResultSet)
				 

				@Override
				public List<String> extractData(ResultSet resultSet)
						throws SQLException, DataAccessException {
					List<String> ids = new ArrayList<String>();
					if (resultSet != null) {
						while (resultSet.next()) {
							ids.add(resultSet.getString(1));
						}
					}

					return ids;
				}
			};

			return getJdbcTemplate().query(
					getQuery(SELECT_BATCH_NODE_JOBS_STARTED), parameters,
					paramTypes, extractor);*/
		    return getNodeStartedJobIds(NODE_NAME);

		} finally {
/*			parameters = null;
			paramTypes = null;
			extractor = null;
*/			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchNodeJobDao.getNodeStartedJobIds() completed");
			}
		}
	}
	
	public List<String> getNodeStartedJobIds(String nodeName) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchNodeJobDao.getNodeStartedJobIds() started");
		}
		Object[] parameters = null;
		int[] paramTypes = null;
		ResultSetExtractor<List<String>> extractor = null;
		try {

			parameters = new Object[] { nodeName };
			paramTypes = new int[] { VARCHAR };
			extractor = new ResultSetExtractor<List<String>>() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.springframework.jdbc.core.ResultSetExtractor#extractData(
				 * java.sql.ResultSet)
				 */

				@Override
				public List<String> extractData(ResultSet resultSet)
						throws SQLException, DataAccessException {
					List<String> ids = new ArrayList<String>();
					if (resultSet != null) {
						while (resultSet.next()) {
							ids.add(resultSet.getString(1));
						}
					}

					return ids;
				}
			};

			return getJdbcTemplate().query(
					getQuery(SELECT_BATCH_NODE_JOBS_STARTED), parameters,
					paramTypes, extractor);

		} finally {
			parameters = null;
			paramTypes = null;
			extractor = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchNodeJobDao.getNodeStartedJobIds() completed");
			}
		}
	}

	/**
	 * De-Link the started executionid with the current node It may be used if
	 * it is defunct
	 * 
	 * @param jobExecutionId
	 */
	public void deLinkNodeStartedJobs(Long jobExecutionId) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchNodeJobDao.deLinkNodeStartedJobs() started");
		}
		/*Object[] parameters = null;
		int[] paramTypes = null;*/
		try {
/*			parameters = new Object[] { NODE_NAME, jobExecutionId };
			paramTypes = new int[] { VARCHAR, NUMERIC };

			getJdbcTemplate().update(getQuery(DELETE_BATCH_NODE_JOBS),
					parameters, paramTypes);*/
		    deLinkNodeStartedJobs(jobExecutionId, NODE_NAME);

		} finally {
/*			parameters = null;
			paramTypes = null;
*/			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchNodeJobDao.deLinkNodeStartedJobs() completed");
			}
		}

	}

	public void deLinkNodeStartedJobs(Long jobExecutionId, String nodeName) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchNodeJobDao.deLinkNodeStartedJobs() started");
		}
		Object[] parameters = null;
		int[] paramTypes = null;
		try {
			parameters = new Object[] { nodeName, jobExecutionId };
			paramTypes = new int[] { VARCHAR, NUMERIC };

			getJdbcTemplate().update(getQuery(DELETE_BATCH_NODE_JOBS),
					parameters, paramTypes);

		} finally {
			parameters = null;
			paramTypes = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchNodeJobDao.deLinkNodeStartedJobs() completed");
			}
		}

	}
}
