/**
 * 
 */
package com.mcg.batch.runtime.impl.item.writers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.mcg.batch.adapter.impl.JdbcAdapter;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class JdbcItemWriter<W> implements ItemWriter<W> {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JdbcItemWriter.class);

	private JdbcAdapter<?, W> adapter;
	private String retryerBeanName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
	 */
	@Override
	public void write(List<? extends W> items) throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JdbcWriter.write() started");
		}
		try {
			adapter.write(items);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JdbcWriter.write() completed");
			}
		}
	}

	/**
	 * @return the adapter JDBCAdapter<?,W>
	 */
	public JdbcAdapter<?, W> getAdapter() {
		return adapter;
	}

	/**
	 * @param adapter
	 *            JDBCAdapter<?,W>
	 */
	public void setAdapter(JdbcAdapter<?, W> adapter) {
		this.adapter = adapter;
	}

	/**
	 * @return the retryerBeanName String
	 */
	public String getRetryerBeanName() {
		return retryerBeanName;
	}

	/**
	 * @param retryerBeanName
	 *            String
	 */
	public void setRetryerBeanName(String retryerBeanName) {
		this.retryerBeanName = retryerBeanName;
	}

}
