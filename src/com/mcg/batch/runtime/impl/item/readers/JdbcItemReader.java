/**
 * 
 */
package com.mcg.batch.runtime.impl.item.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.mcg.batch.adapter.impl.JdbcAdapter;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class JdbcItemReader<R> implements ItemStreamReader<R> {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JdbcItemReader.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemReader#read()
	 */

	JdbcAdapter<R, ?> adapter;

	@SuppressWarnings("unchecked")
	@Override
	public R read() throws Exception, UnexpectedInputException, ParseException,
			NonTransientResourceException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JDBCReader.read() started");
		}
		try {
			return ((R) adapter.invoke("read", Object.class));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JDBCReader.read() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.item.ItemStream#open(org.springframework.batch
	 * .item.ExecutionContext)
	 */
	@Override
	public void open(ExecutionContext executionContext)
			throws ItemStreamException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JdbcItemReader.open() started");
		}
		try {
			adapter.getReader().open(executionContext);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JdbcItemReader.open() completed");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemStream#close()
	 */
	@Override
	public void close() throws ItemStreamException {
		adapter.getReader().close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.item.ItemStream#update(org.springframework.
	 * batch.item.ExecutionContext)
	 */
	@Override
	public void update(ExecutionContext executionContext)
			throws ItemStreamException {
		adapter.getReader().update(executionContext);
	}

	/**
	 * @return the adapter JDBCAdapter<R,?>
	 */
	public JdbcAdapter<R, ?> getAdapter() {
		return adapter;
	}

	/**
	 * @param adapter
	 *            JDBCAdapter<R,?>
	 */
	public void setAdapter(JdbcAdapter<R, ?> adapter) {
		this.adapter = adapter;
	}

}
