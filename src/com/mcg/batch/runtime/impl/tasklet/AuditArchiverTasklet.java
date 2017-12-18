/**
 * 
 */
package com.mcg.batch.runtime.impl.tasklet;

import static com.mcg.batch.utils.StringHelper.COMMA_CHAR;
import static com.mcg.batch.utils.StringHelper.concat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.mcg.batch.core.ContextFactory;
import com.mcg.batch.utils.StringHelper;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class AuditArchiverTasklet implements StoppableTasklet {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AuditArchiverTasklet.class);

	private String sourceDsName;
	private String targetDsName;
	private String retainDays = "60";
	private int retainDaysInt = -1;
	private int commitIntervalInt = -1;
	private String commitInterval = "1000";
	private String tables = null;
	private boolean stop = false;
	private boolean sourceDelAutoCommit = true;
	private boolean targetAutoCommit = true;

	private static final String[] TABLES = new String[] { "BATCH_AUDIT_EVENT",
			"BATCH_EXCEPTION_EVENT", "BATCH_SCHEDULE_AUDIT",
			"BATCH_SCHEDULE_EXCEPTION" };

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.
	 * springframework.batch.core.StepContribution,
	 * org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1)
			throws Exception {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("The value of retention is " + getRetainDaysInt());
			LOGGER.debug("The value of commitInterval  is "
					+ getCommitInterval());

			LOGGER.debug("The value of tables  is " + this.tables);
		}

		DataSource sourceDS = ContextFactory.getInstance().aquireContext()
				.getBean(sourceDsName, DataSource.class);
		DataSource targetDS = ContextFactory.getInstance().aquireContext()
				.getBean(targetDsName, DataSource.class);
		PreparedStatement sourcePs = null;
		PreparedStatement sourceDelPs = null;
		PreparedStatement targetPs = null;
		Connection sourceCon = null;
		Connection sourceDelCon = null;

		Connection targetCon = null;
		ResultSet rs = null;
		String[] tableList = null;
		ResultSetMetaData rsmd = null;

		int totalupdates = 0;
		int batchSize = 0;
		boolean executeUpdate = false;

		try {
			sourceCon = sourceDS.getConnection();
			targetCon = targetDS.getConnection();
			sourceDelCon = sourceDS.getConnection();
			sourceDelAutoCommit = sourceDelCon.getAutoCommit();
			targetAutoCommit = targetCon.getAutoCommit();

			sourceDelCon.setAutoCommit(false);
			targetCon.setAutoCommit(false);
			tableList = getTableList();

			for (int i = 0; i < tableList.length & !stop; i++) {
				executeUpdate = false;
				batchSize = 0;
				totalupdates = 0;
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The Select query is "
							+ getSourceQuery(tableList[i]));
					LOGGER.debug("The Delete query is "
							+ getSourceDelQuery(tableList[i]));

				}

				sourcePs = sourceCon
						.prepareStatement(getSourceQuery(tableList[i]));
				sourcePs.setInt(1, getRetainDaysInt());
				rs = sourcePs.executeQuery();
				rsmd = rs.getMetaData();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The Insert query is "
							+ createInsertQuery(tableList[i],
									rsmd.getColumnCount()));
				}
				targetPs = targetCon.prepareStatement(createInsertQuery(
						tableList[i], rsmd.getColumnCount()));

				sourceDelPs = sourceDelCon
						.prepareStatement(getSourceDelQuery(tableList[i]));
				if (rs.next()) {
					executeUpdate = true;
					totalupdates++;
					batchSize++;

					for (int j = 1; j <= rsmd.getColumnCount(); j++) {
						targetPs.setObject(j, rs.getObject(j),
								rsmd.getColumnType(j));
					}
					if (sourceDelPs != null) {
						sourceDelPs.setLong(1,
								rs.getLong(getIDString(tableList[i])));
					}
					targetPs.addBatch();
					sourceDelPs.addBatch();
					batchSize += 1;
				}

				while (rs.next() && !stop) {
					executeUpdate = true;
					batchSize += 1;
					totalupdates++;
					if (targetPs != null && sourceDelPs != null) {
						for (int j = 1; j <= rsmd.getColumnCount(); j++) {
							targetPs.setObject(j, rs.getObject(j),
									rsmd.getColumnType(j));
						}

						sourceDelPs.setLong(1,
								rs.getLong(getIDString(tableList[i])));
						targetPs.addBatch();
						sourceDelPs.addBatch();
						// updateCount = targetPs.executeUpdate();
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("batch size-" + batchSize);
						}
						if (batchSize % getCommitIntervalInt() == 0) {
							targetPs.executeBatch();
							sourceDelPs.executeBatch();
							targetCon.commit();
							sourceDelCon.commit();
							executeUpdate = false;
							batchSize = 0;

						}
					}

				}
				if (executeUpdate) {
					if (targetPs != null && sourceDelPs != null) {
						targetPs.executeBatch();
						sourceDelPs.executeBatch();
					}
					targetCon.commit();
					sourceDelCon.commit();
				}

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The Number of updates is " + totalupdates);
				}

				// targetCon.commit();
				//
				// sourceDelPs.executeUpdate();
				// sourceDelCon.commit();

				ThreadContextUtils.addToExecutionContext(
						concat("Archival Completed for the table ",
								tableList[i], " and total update count ="),
						totalupdates);
				close(rs);
				rs = null;
				close(sourcePs);
				sourcePs = null;
				close(sourceDelPs);
				sourceDelPs = null;
				close(targetPs);
				targetPs = null;

			}
		} catch (SQLException e) {
			LOGGER.error("Exception Occured...", e);
			throw e;
		} finally {
			close(rs);
			rs = null;
			close(sourcePs);
			sourcePs = null;
			close(sourceDelPs);
			sourceDelPs = null;
			close(targetPs);
			targetPs = null;
			if (sourceDelCon != null) {
				sourceDelCon.setAutoCommit(sourceDelAutoCommit);
			}
			if (targetCon != null) {
				targetCon.setAutoCommit(targetAutoCommit);
			}
			close(sourceCon);
			close(sourceDelCon);
			close(targetCon);
			sourceCon = null;
			targetCon = null;
			rsmd = null;

		}

		return RepeatStatus.FINISHED;
	}

	@SuppressWarnings("unused")
	private static int getUpdateCount(int[] updates) {
		int count = 0;

		if (updates != null) {
			for (int i = 0; i < updates.length; i++) {
				if (updates[i] > 0) {
					count += updates[i];
				}

			}
		}
		return count;
	}

	public static void main(String[] args) {
		System.out.println(1 % -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.step.tasklet.StoppableTasklet#stop()
	 */
	@Override
	public void stop() {
		this.stop = true;

	}

	private String getSourceQuery(String tableName) {
		String query = "SELECT * FROM " + tableName
				+ " where TIMESTAMP <(SYSDATE - ?) AND ";
		if ("BATCH_SCHEDULE_AUDIT".equalsIgnoreCase(tableName)
				|| "BATCH_SCHEDULE_EXCEPTION".equalsIgnoreCase(tableName)) {
			query = query + " BATCH_NAME LIKE '"
					+ ThreadContextUtils.getNamespace() + "%'";
		} else {
			query = query + "DOMAIN ='" + ThreadContextUtils.getNamespace()
					+ "'";
		}

		return query;

	}

	private String getSourceDelQuery(String tableName) {

		return "DELETE  FROM " + tableName + " where " + getIDString(tableName)
				+ " = ?";
	}

	private String getIDString(String tableName) {
		return tableName.substring("BATCH_".length()) + "_ID";
	}

	private String createInsertQuery(String tableName, int coulumnCount) {

		StringBuilder builder = new StringBuilder("INSERT INTO ");
		builder.append(tableName);
		builder.append(" VALUES (");
		for (int i = 0; i < coulumnCount; i++) {
			builder.append('?');
			if (i != coulumnCount - 1) {
				builder.append(COMMA_CHAR);
			}
		}
		builder.append(')');

		return builder.toString();
	}

	/**
	 * @return the sourceDsName String
	 */
	public String getSourceDsName() {
		return sourceDsName;
	}

	/**
	 * @param sourceDsName
	 *            String
	 */
	public void setSourceDsName(String sourceDsName) {
		this.sourceDsName = sourceDsName;
	}

	/**
	 * @return the targetDsName String
	 */
	public String getTargetDsName() {
		return targetDsName;
	}

	/**
	 * @param targetDsName
	 *            String
	 */
	public void setTargetDsName(String targetDsName) {
		this.targetDsName = targetDsName;
	}

	/**
	 * @return the retainDays String
	 */
	public String getRetainDays() {
		return retainDays;
	}

	/**
	 * @param retainDays
	 *            String
	 */
	public void setRetainDays(String retainDays) {
		this.retainDays = retainDays;
	}

	/**
	 * @return the retainDaysInt int
	 */
	public int getRetainDaysInt() {
		if (retainDaysInt == -1) {
			retainDaysInt = Integer.parseInt(retainDays);
		}
		return retainDaysInt;
	}

	public String[] getTableList() {
		if (tables == null) {
			return TABLES;
		} else {
			return StringHelper.delimitedStringToArray(this.tables, COMMA_CHAR);
		}

	}

	/**
	 * @return the tables String
	 */
	public String getTables() {

		return tables;
	}

	/**
	 * @param tables
	 *            String
	 */

	public void setTables(String tables) {
		this.tables = tables;
	}

	private static final void close(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(
							"Exception in closing the statement .. continuing ",
							e);
				}
			} finally {
				statement = null;
			}
		}
	}

	private static final void close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(
							"Exception in closing the connection .. continuing ",
							e);
				}
			} finally {
				connection = null;
			}
		}
	}

	private static final void close(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(
							"Exception in closing the resultSet .. continuing ",
							e);
				}
			} finally {
				resultSet = null;
			}
		}
	}

	/**
	 * @return the commitInterval String
	 */
	public String getCommitInterval() {
		return commitInterval;
	}

	/**
	 * @param commitInterval
	 *            String
	 */
	public void setCommitInterval(String commitInterval) {
		this.commitInterval = commitInterval;
	}

	/**
	 * @return the commitIntervalInt int
	 */
	public int getCommitIntervalInt() {
		if (commitIntervalInt == -1) {
			commitIntervalInt = Integer.parseInt(commitInterval);
		}
		return commitIntervalInt;
	}

}
