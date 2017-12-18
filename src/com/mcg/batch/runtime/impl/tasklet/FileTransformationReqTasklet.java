/**
 * 
 */
package com.mcg.batch.runtime.impl.tasklet;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.mcg.batch.adapter.impl.JMSAdapter;
import com.mcg.batch.events.BatchTransalationEvent;
import com.mcg.batch.events.support.EventBuilder;
import com.mcg.batch.utils.StringHelper;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class FileTransformationReqTasklet implements Tasklet {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileTransformationReqTasklet.class);
	private JMSAdapter requestAdapter;
	private String requestDestination;
	private String requestDestinationType;
	private String functionName;
	private String params;
	private LinkedHashMap<String, String> parameters;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.
	 * springframework.batch.core.StepContribution,
	 * org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("TransformationRequestTasklet.execute() started");
		}
		BatchTransalationEvent requestEvent = null;
		try {
			requestEvent = EventBuilder.createRequestEvent(functionName,
					ThreadContextUtils.getJobInstanceIdAsString(), parameters);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The request event is " + requestEvent);
			}
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put("batchName", ThreadContextUtils.getJobExecution().getJobInstance().getJobName());
			requestAdapter.invoke(JMSAdapter.SEND_MESSAGE_WITH_PROP, null,
					requestDestination, requestEvent, properties);
			/*requestAdapter.invoke(JMSAdapter.SEND_MESSAGE, null,
					requestDestination, requestEvent);*/
			return RepeatStatus.FINISHED;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("TransformationRequestTasklet.execute() completed");
			}
		}

	}

	/**
	 * @return the requestAdapter JMSAdapter
	 */
	public JMSAdapter getRequestAdapter() {
		return requestAdapter;
	}	

	/**
	 * @param requestAdapter
	 *            JMSAdapter
	 */
	public void setRequestAdapter(JMSAdapter requestAdapter) {
		this.requestAdapter = requestAdapter;
	}

	/**
	 * @return the requestDestination String
	 */
	public String getRequestDestination() {
		return requestDestination;
	}

	/**
	 * @param requestDestination
	 *            String
	 */
	public void setRequestDestination(String requestDestination) {
		this.requestDestination = requestDestination;
	}

	/**
	 * @return the functionName String
	 */
	public String getFunctionName() {
		return functionName;
	}

	/**
	 * @param functionName
	 *            String
	 */
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	/**
	 * @return the parameters LinkedHashMap<String,String>
	 */
	public LinkedHashMap<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            LinkedHashMap<String,String>
	 */
	public void setParameters(LinkedHashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the params String
	 */
	public String getParams() {
		return params;
	}

	/**
	 * @param params
	 *            String
	 */
	public void setParams(String params) {
		this.params = params;
		if (params != null) {
			this.parameters = new LinkedHashMap<String, String>(
					(StringHelper.delimitedStringsToMap(params,';')));
		}
	}

	/**
	 * Getter for requestDestinationType
	 *
	 * @return requestDestinationType String
	 */
	public String getRequestDestinationType() {
	    return requestDestinationType;
	}

	/**
	 * Setter for requestDestinationType
	 *
	 * @param requestDestinationType String
	 */
	public void setRequestDestinationType(String requestDestinationType) {
	    this.requestDestinationType = requestDestinationType;
	}
}
