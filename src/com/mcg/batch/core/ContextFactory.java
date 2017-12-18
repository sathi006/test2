/**
 * 
 */
package com.mcg.batch.core;

import static com.mcg.batch.core.BatchConfiguration.BATCH_APPLICATION_CONTEXT_FILE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;

/**
 * 
 * A Singleton factory to get the root Application context and the
 * BatchApplicationContext
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class ContextFactory {
	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ContextFactory.class);


	private static ApplicationContext applicationContext;

	/**
	 * prevent external instantiation
	 */
	private ContextFactory() {
		try{
		init();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			LOGGER.error("Debug not enabled");
		}
	}

	/**
	 * 
	 */
	private void init() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ContextFactory.init() started ");
		}
		try{
			applicationContext = new FileSystemXmlApplicationContext(BATCH_APPLICATION_CONTEXT_FILE);
		}
		catch(Throwable e){
			e.printStackTrace();
		}
		finally{
			LOGGER.error("Debug not enabled");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ContextFactory.init() completed with applicationcontext " + (applicationContext==null?"null":"not null"));
		}
		else{
			LOGGER.error("ContextFactory.init() completed with applicationcontext " + (applicationContext==null?"null":"not null"));
			LOGGER.error("Debug not enabled");

		}

	}

	/**
	 * Lazy init singleton holder
	 * 
	 * @version 1.0
	 * @since:1.0
	 * @author Nanda Gopalan
	 *
	 */
	private static final class ContextFactoryInner {
		private static final ContextFactory INSTANCE = new ContextFactory();
	}

	public static final ContextFactory getInstance() {
		return ContextFactoryInner.INSTANCE;
	}

	/**
	 * Provide the {@link ApplicationContext} for a given namespace
	 * 
	 * @param namespace
	 * @return
	 */
	// public final ApplicationContext aquireContext(String namespace) {
	// return NAME_SPACE_CONTEXTS.get(namespace);
	// }

	public final ApplicationContext aquireContext() {
		return applicationContext;
	}

	public final ApplicationContextFactory aquireBatchContextFactory(
			byte[] batchXML) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ContextFactory.aquireBatchContextFactory() started ");
		}
		GenericApplicationContextFactory factory = new GenericApplicationContextFactory(
				new ByteArrayResource(batchXML));
		factory.setApplicationContext(aquireContext());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ContextFactory.aquireBatchContextFactory() completed");
		}
		return factory;
	}

	public final DefaultListableBeanFactory getBeanFactory() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ContextFactory.getBeanFactory() started");
		}
		try {
			return (DefaultListableBeanFactory) aquireContext()
					.getAutowireCapableBeanFactory();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("ContextFactory.getBeanFactory() completed");
			}
		}
	}

	public final void shutdown() {
		((AbstractApplicationContext) aquireContext()).close();
	}
}
