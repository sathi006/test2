/**
 * 
 */
package com.mcg.batch.runtime.impl.batch.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcg.batch.core.BatchConfiguration;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class EncryptedPropertyAccessor {
	/**
	 * Logger to be used by this class.
	 */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory
			.getLogger(EncryptedPropertyAccessor.class);

	public static final String decryptProperty(String key) {
		return EncryptorFactory.doTextDecryption(BatchConfiguration
				.getProperty(key));
	}
}
