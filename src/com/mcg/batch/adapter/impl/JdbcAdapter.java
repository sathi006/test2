/**
 * 
 */
package com.mcg.batch.adapter.impl;

import static com.mcg.batch.core.BatchConfiguration.BATCH_FAULTY_RESOURCE;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.util.Assert;

import com.mcg.batch.exceptions.AdapterException;
import com.mcg.batch.exceptions.NonRetryableExecption;
import com.mcg.batch.exceptions.RetryableException;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */

public class JdbcAdapter<R, W> extends BaseBatchAdapter<DataSource> {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JdbcAdapter.class);

	private JdbcBatchItemWriter<W> writer = null;
	private JdbcCursorItemReader<R> reader = null;

	public R read() throws AdapterException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JdbcAdapter.read() started");
		}
		try {
			Assert.notNull(reader, "The reader is not setup or is null");
			return reader.read();
		} catch (UnexpectedInputException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, "JDBCSource");
			throw new NonRetryableExecption(e);
		} catch (ParseException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, "JDBCSource");
			throw new NonRetryableExecption(e);
		} catch (SQLException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, "JDBCSource");
			throw new RetryableException(e);
		} catch (Exception e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, "JDBCSource");
			throw new NonRetryableExecption(e);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JdbcAdapter.read() completed");
			}
		}

	}

	public void write(final List<? extends W> items) throws AdapterException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JdbcAdapter.write() started");
		}
		try {
			Assert.notNull(writer, "The writer is not setup or is null");
			getWriter().write(items);
		} catch (SQLException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, "JDBCTarget");
			throw new RetryableException(e);
		} catch (Exception e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, "JDBCTarget");
			throw new NonRetryableExecption(e);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JdbcAdapter.write() completed");
			}
		}
	}



	/**
	 * @return the writer JdbcBatchItemWriter<W>
	 */
	public JdbcBatchItemWriter<W> getWriter() {
		return writer;
	}

	/**
	 * @param writer
	 *            JdbcBatchItemWriter<W>
	 */
	public void setWriter(final JdbcBatchItemWriter<W> writer) {
		this.writer = writer;
	}

	/**
	 * @return the reader JdbcCursorItemReader<R>
	 */
	public JdbcCursorItemReader<R> getReader() {
		return reader;
	}

	/**
	 * @param reader
	 *            JdbcCursorItemReader<R>
	 */
	public void setReader(final JdbcCursorItemReader<R> reader) {
		this.reader = reader;
	}

}
