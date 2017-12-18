/**
 * 
 */
package com.mcg.batch.utils;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class StringHolder implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6048265061711835527L;

    /**
     * Logger to be used by this class.
     */
    private static final Logger LOGGER = LoggerFactory
	    .getLogger(StringHolder.class);

    private String value;
    
    public StringHolder() {
	
    }

    /**
     * @param value
     */
    public StringHolder(String value) {
	super();
	this.value = value;
    }

    /**
     * @return the value String
     */
    public String getValue() {
	return value;
    }

    /**
     * @param value
     *            String
     */
    public void setValue(String value) {
	this.value = value;
    }

    /**
     * Create a new string holder
     * 
     * @param value
     * @return
     */
    public static final StringHolder create(String value) {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Creation of StringHolder Started");
	}
	ClassLoader old = Thread.currentThread().getContextClassLoader();
	try {

	    Thread.currentThread().setContextClassLoader(
		    StringHolder.class.getClassLoader());
	    return new StringHolder(value);

	} finally {
	    Thread.currentThread().setContextClassLoader(old);
	    if (LOGGER.isTraceEnabled()) {
		    LOGGER.trace("Creation of StringHolder Finished");
		}
	}

    }

}
