/**
 * 
 */
package com.mcg.batch.audit;

import static com.mcg.batch.core.BatchWiringConstants.AUDIT_LOGGER_JMS_ADAPTER;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_JMS_LOGGER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_RT_STORE_COMPONENT;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.mcg.batch.adapter.impl.JMSAdapter;
import com.mcg.batch.core.BatchConfiguration;
import com.mcg.batch.events.BatchAuditEvent;
import com.mcg.batch.events.BatchNotificationEvent;
import com.mcg.batch.events.Event;
import com.mcg.batch.exceptions.AdapterException;
import com.mcg.batch.store.BatchRuntimeStore;
import com.mcg.batch.utils.ConvertEventToXML;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@Component(BATCH_JMS_LOGGER_COMPONENT)
public class BatchAuditJMSLogger implements BatchAuditLogger {
	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchAuditJMSLogger.class);

	@Autowired
	@Qualifier(AUDIT_LOGGER_JMS_ADAPTER)
	JMSAdapter jmsAdapter;
	
	@Autowired
	@Qualifier(BATCH_RT_STORE_COMPONENT)
	BatchRuntimeStore batchStore;
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.audit.BatchAuditLogger#fatal(com.mcg.batch.events
	 * .Event)
	 */
	@Override
	public void fatal(Event<?, ?> event) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchAuditJMSLogger.fatal() started");
		}

		try {
			jmsAdapter.invoke(JMSAdapter.SEND_MESSAGE, null,
					BatchConfiguration.AUDIT_ERROR_DESTINATION, event);
		} catch (AdapterException e) {
			LOGGER.error(
					"Unable to send the message to error destination event details:\n"
							+ event, e);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchAuditJMSLogger.fatal() completed");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.audit.BatchAuditLogger#info(com.mcg.batch.events
	 * .Event)
	 */
	
	@Override
	public void info(Event<?, ?> event) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchAuditJMSLogger.info() started");
		}
		try {
			Long executionId = ThreadContextUtils.getJobExecutionId();
			JobExecution jobExecution = batchStore.getJobExecution(executionId);
			String batchName = jobExecution.getJobInstance().getJobName();
			BatchStatus batchStatus = jobExecution.getStatus();
			
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put("batchName", batchName);
			properties.put("batchStatus", batchStatus.toString());
			
			jmsAdapter.invoke(JMSAdapter.SEND_MESSAGE, null,
					BatchConfiguration.AUDIT_INFO_DESTINATION, event);
			
			String xmlString = ConvertEventToXML.convertEventToXML(event);
			jmsAdapter.invoke(JMSAdapter.SEND_MESSAGE_WITH_PROP, null,
					BatchConfiguration.NOTIFICATION_DESTINATION, xmlString, properties);
		} catch (AdapterException e) {
			LOGGER.error(
					"Unable to send the message to info destination event details:\n"
							+ event, e);

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchAuditJMSLogger.info() completed");
			}
		}
	}
	
	@Override
	public void info(Event<?, ?> event, JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchAuditJMSLogger.info() started");
		}
		try {
			if(event instanceof BatchAuditEvent){
				jmsAdapter.invoke(JMSAdapter.SEND_MESSAGE, null,
						BatchConfiguration.AUDIT_INFO_DESTINATION, event);
			}else if(event instanceof BatchNotificationEvent){
				String batchName = jobExecution.getJobInstance().getJobName();
				BatchStatus batchStatus = jobExecution.getStatus();
				HashMap<String, Object> properties = new HashMap<String, Object>();
				properties.put("batchName", batchName);
				properties.put("batchStatus", batchStatus.toString());
				String xmlString = ConvertEventToXML.convertEventToXML(event);
				jmsAdapter.invoke(JMSAdapter.SEND_MESSAGE_WITH_PROP, null,
						BatchConfiguration.NOTIFICATION_DESTINATION, xmlString, properties);
			}
			
		} catch (AdapterException e) {
			LOGGER.error(
					"Unable to send the message to info destination event details:\n"
							+ event, e);

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchAuditJMSLogger.info() completed");
			}
		}
	}
	


}
