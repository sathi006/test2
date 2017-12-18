/**
 * 
 */
package com.mcg.batch.runtime.impl.item.writers;

import static com.mcg.batch.adapter.impl.JMSAdapter.SEND_MESSAGES;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.mcg.batch.adapter.impl.JMSAdapter;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class JMSItemWriter implements ItemWriter<Serializable> {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JMSItemWriter.class);

	private JMSAdapter adapter;
	private String destination;

	public void write(List<? extends Serializable> items) throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("JMSItemWriter.write() started");
		}

		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Writing the items "+items);
				LOGGER.debug("Items Class Name is "+items.getClass().getName());
			}
			adapter.invoke(SEND_MESSAGES, null, destination, items);
			
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("JMSItemWriter.write() completed");
			}
		}
	}

	/**
	 * @return the adapter JMSAdapter
	 */
	public JMSAdapter getAdapter() {
		return adapter;
	}

	/**
	 * @param adapter
	 *            JMSAdapter
	 */
	public void setAdapter(JMSAdapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * @return the destination String
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * @param destination
	 *            String
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

}
