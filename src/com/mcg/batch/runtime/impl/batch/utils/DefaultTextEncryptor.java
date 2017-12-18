/**
 * 
 */
package com.mcg.batch.runtime.impl.batch.utils;

import org.jasypt.util.text.BasicTextEncryptor;
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
public class DefaultTextEncryptor implements Encryptor<String, String> {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultTextEncryptor.class);

	private BasicTextEncryptor textEncryptor = null;

	
	public DefaultTextEncryptor(String key){
		textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(key);
	}
	
	/**
	 * 
	 */
	public DefaultTextEncryptor() {
		this(BatchConfiguration.ENCRYPTION_KEY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.runtime.impl.batch.utils.Encryptor#decrypt(java.lang
	 * .Object)
	 */
	@Override
	public String decrypt(String encryptionObject) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("DefaultTextEncryptor.decrypt() started");
		}
		try {
			return textEncryptor.decrypt(encryptionObject);

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("DefaultTextEncryptor.decrypt() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.runtime.impl.batch.utils.Encryptor#encrypt(java.lang
	 * .String)
	 */
	@Override
	public String encrypt(String input) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("DefaultTextEncryptor.encrypt() started");
		}
		try {
			return textEncryptor.encrypt(input);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("DefaultTextEncryptor.encrypt() completed");
			}
		}

	}
}
