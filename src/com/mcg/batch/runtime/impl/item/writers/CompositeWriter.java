/**
 * 
 */
package com.mcg.batch.runtime.impl.item.writers;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

/**
 * This class can be used for writing items to multiple target destinations.
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class CompositeWriter implements ItemWriter<Serializable> {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CompositeWriter.class);

	List<ItemWriter<Serializable>> itemWriters;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
	 */
	@Override
	public void write(List<? extends Serializable> items) throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("CompositeWriter.write() started");
		}
		try {
			for (ItemWriter<Serializable> itemWriter : itemWriters) {
				itemWriter.write(items);
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("CompositeWriter.write() completed");
			}
		}

	}

	/**
	 * @param itemWriters
	 *            List<ItemWriter<Serializable>>
	 */
	public void setItemWriters(List<ItemWriter<Serializable>> itemWriters) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("CompositeWriter.setItemWriters() started");
		}
		try {
			this.itemWriters = itemWriters;

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("CompositeWriter.setItemWriters() completed");
			}
		}

	}

}
