/**
 * 
 */
package com.mcg.batch.utils;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class CustomInitialContextFactory implements InitialContextFactory {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CustomInitialContextFactory.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable
	 * )
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Context getInitialContext(Hashtable env) throws NamingException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("CustomInitialContextFactory.getInitialContext() started");
		}
		try {
			return new CustomJmsNamingContext(env);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("CustomInitialContextFactory.getInitialContext() completed");
			}
		}

	}
}
