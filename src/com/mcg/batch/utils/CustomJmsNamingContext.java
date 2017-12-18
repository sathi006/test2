/**
 * 
 */
package com.mcg.batch.utils;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.naming.Name;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webmethods.jms.WmConnectionFactory;
import com.webmethods.jms.naming.WmJmsNamingContext;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class CustomJmsNamingContext extends WmJmsNamingContext {

	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CustomJmsNamingContext.class);

	private static final  String CLIENT_ID_LABEL="com.webmethods.jms.naming.clientid";
	
	private String clientId=null;
	
	/**
	 * @param env
	 * @param contextStoreName
	 * @throws NamingException
	 */
	@SuppressWarnings("rawtypes")
	protected CustomJmsNamingContext(Hashtable env, String contextStoreName)
			throws NamingException {
		super(env, contextStoreName);
		clientId=(String)env.get(CLIENT_ID_LABEL);
		
	}

	/**
	 * @param env
	 * @throws NamingException
	 */
	public CustomJmsNamingContext(Hashtable<?, ?> env) throws NamingException {
		super(env);
		clientId=(String)env.get(CLIENT_ID_LABEL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.webmethods.jms.naming.WmJmsNamingContext#lookup(javax.naming.Name)
	 */
	@Override
	public Object lookup(Name arg0) throws NamingException {
		Object object = super.lookup(arg0);
		if (object instanceof WmConnectionFactory) {
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Setting the client id value as "+clientId);
				}
				
				((WmConnectionFactory) object).setClientID(null);
			} catch (JMSException e) {
				LOGGER.error("Exception while setting client id... ", e);
			}
		}
		return object;
	}

}
