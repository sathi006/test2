/**
 * 
 */
package com.mcg.batch.test.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class TestParameter {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TestParameter.class);

	public static final String getFileName() {

		return "*.txt";
	}
}
