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
public class EncryptorFactory {

	private static Encryptor<String, String> textEncryptor = null;
	private static final Object LOCK_OBJECT = new Object();

	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(EncryptorFactory.class);

	/**
	 * prevent external instantiation
	 */
	private EncryptorFactory() {
	}

	public static final Encryptor<String, String> getTextEncryptor() {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("EncryptorFactory.getTextEncryptor() started");
		}
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The Encryptor at begining is " + textEncryptor);
			}
			if (textEncryptor == null) {
				try {
					synchronized (LOCK_OBJECT) {
						if (textEncryptor == null) {
							textEncryptor = BatchConfiguration.TEXT_ENCRYPTOR_CLASS
									.newInstance();
						}
					}

				} catch (InstantiationException e) {
					LOGGER.error("Error while loading the  encryptor " + e);
				} catch (IllegalAccessException e) {
					LOGGER.error("Error while loading the  encryptor " + e);
				}
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The Encryptor at end is " + textEncryptor);
			}
			return textEncryptor;

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("EncryptorFactory.getTextEncryptor() completed");
			}
		}

	}
	


	/**
	 * The do text encryption using the configured 	 * 
	 * @param text
	 * @return
	 */
	public static final String doTextEncryption(String text) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("EncryptorFactory.doTextEncryption() started");
		}
		try {
	
			return getTextEncryptor().encrypt(text);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("EncryptorFactory.doTextEncryption() completed");
			}
		}
	}

	public static final String doTextDecryption(String text) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("EncryptorFactory.doTextDecryption() started");
		}
		try {
		
			return getTextEncryptor().decrypt(text);
		} finally {
			
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("EncryptorFactory.doTextDecryption() completed");
			}
		}
	}
}
