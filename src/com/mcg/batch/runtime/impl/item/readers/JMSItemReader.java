/**
 * 
 */
package com.mcg.batch.runtime.impl.item.readers;

import static com.mcg.batch.adapter.impl.JMSAdapter.REC_AND_CONVERT_SELECTED;
import static com.mcg.batch.adapter.impl.JMSAdapter.REC_DS_AND_CONVERT_SELECTED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.mcg.batch.adapter.impl.JMSAdapter;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * @param <R>
 * 
 */
public class JMSItemReader implements ItemReader<Object> {
    /**
     * Logger to be used by this class.
     */
    private static final Logger LOGGER = LoggerFactory
	    .getLogger(JMSItemReader.class);

    private static final String TOPIC_KEY = "topic";
    private static Class<?> OBJECT_CLASS = null;

    static {
	try {
	    OBJECT_CLASS = Class.forName("java.lang.Object");
	} catch (ClassNotFoundException e) {
	    if (LOGGER.isErrorEnabled()) {
		LOGGER.error("Unable to find Object class", e);
	    }
	}
    }

    private JMSAdapter adapter;
    private String destination;
    private String durableSubscriberName;
    private String destinationType;
    private String filter;
    private long receiveTimeout;
    private int ackMode;
    private int maxMessageCount;
    private int readMessageCount;

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.batch.item.ItemReader#read()
     */
    @Override
    public Object read() throws Exception, UnexpectedInputException,
	    ParseException, NonTransientResourceException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("JMSItemReader.read() started");
	}

	Object item = null;
	try {
	    if (ackMode != 0 && ackMode >= 1 && ackMode <= 3) {
		adapter.getResource().setSessionAcknowledgeMode(ackMode);
	    }
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("ackMode : " + ackMode);
	    }

	    if (maxMessageCount > 0) {
		ThreadContextUtils.addToExecutionContext("maxMessageCount",
			maxMessageCount);
		if (ThreadContextUtils.getExecutionContext().containsKey(
			"readMessageCount")) {
		    readMessageCount = ThreadContextUtils.getExecutionContext()
			    .getInt("readMessageCount");
		}
	    }
	    if (TOPIC_KEY.equalsIgnoreCase(destinationType)
		    && durableSubscriberName != null) {
		item = (Object) adapter.invoke(REC_DS_AND_CONVERT_SELECTED,
			OBJECT_CLASS, destination, durableSubscriberName,
			receiveTimeout, filter == null ? "" : filter);
	    } else {
	    	adapter.getResource().setSessionAcknowledgeMode(1);
		item = (Object) adapter.invoke(REC_AND_CONVERT_SELECTED,
			OBJECT_CLASS, destination,
			receiveTimeout <= 0 ? adapter.getResource()
				.getReceiveTimeout() : receiveTimeout,
			(filter == null ? "" : filter));
	    }
	    if (maxMessageCount > 0) {
		if (item != null) {
		    readMessageCount++;
		    ThreadContextUtils.addToExecutionContext(
			    "readMessageCount", readMessageCount);
		}
	    }
	    return item;
	} finally {
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("JMSItemReader.read() completed");
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

    /**
     * @return the durableSubscriberName String
     */
    public String getDurableSubscriberName() {
	return durableSubscriberName;
    }

    /**
     * @param durableSubscriberName
     *            String
     */
    public void setDurableSubscriberName(String durableSubscriberName) {
	this.durableSubscriberName = durableSubscriberName;
    }

    /**
     * @return the destinationType String
     */
    public String getDestinationType() {
	return destinationType;
    }

    /**
     * @param destinationType
     *            String
     */
    public void setDestinationType(String destinationType) {
	this.destinationType = destinationType;
    }

    /**
     * @return the filter String
     */
    public String getFilter() {
	return filter;
    }

    /**
     * @param filter
     *            String
     */
    public void setFilter(String filter) {
	this.filter = filter;
    }

    /**
     * @return the receiveTimeout long
     */
    public long getReceiveTimeout() {
	return receiveTimeout;
    }

    /**
     * @param receiveTimeout
     *            long
     */
    public void setReceiveTimeout(long receiveTimeout) {
	this.receiveTimeout = receiveTimeout;
    }

    public int getAckMode() {
	return ackMode;
    }

    public int getMaxMessageCount() {
	return maxMessageCount;
    }

    public void setMaxMessageCount(int maxMessageCount) {
	this.maxMessageCount = maxMessageCount;
    }

    public void setAckMode(int ackMode) {
	this.ackMode = ackMode;
    }

}
