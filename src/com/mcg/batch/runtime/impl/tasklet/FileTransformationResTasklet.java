/**
 * 
 */
package com.mcg.batch.runtime.impl.tasklet;

import static com.mcg.batch.utils.StringHelper.QUOTE_CHAR;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.mcg.batch.adapter.impl.JMSAdapter;
import com.mcg.batch.adapters.impl.support.vfs.BatchVFSParameters;
import com.mcg.batch.events.BatchTransalationEvent;
import com.mcg.batch.exceptions.BatchException;
import com.mcg.batch.utils.StringHelper;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class FileTransformationResTasklet implements Tasklet {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileTransformationResTasklet.class);
	private JMSAdapter responseAdapter;
	private String responseDestination;
	private String responseDestinationType;
	private String responseDestinationDS;
	private long timeout;
	private VFSTasklet vfsTasklet;
	private static final String CORRELATION_ID_FILTER_PREFIX = "JMSCorrelationID='";
	
	private static final String TOPIC_TYPE = "Topic";

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
		BatchTransalationEvent responseEvent;
		BatchVFSParameters params = null;
		String filter = null;
		try {

			filter = StringHelper.concatNotNulls(CORRELATION_ID_FILTER_PREFIX,
					ThreadContextUtils.getJobInstanceIdAsString(), QUOTE_CHAR);
			if (LOGGER.isDebugEnabled()) {
			    LOGGER.debug("Filter to be used : " + filter);
			}
			if (TOPIC_TYPE.equalsIgnoreCase(responseDestinationType)) {
			    responseEvent = responseAdapter
					.invoke(JMSAdapter.REC_DS_AND_CONVERT_SELECTED,
							BatchTransalationEvent.class, responseDestination, responseDestinationDS,
							timeout > 0 ? timeout : responseAdapter
									.getResource().getReceiveTimeout(), (filter
									.length() == CORRELATION_ID_FILTER_PREFIX
									.length() + 1 ? StringHelper.EMPTY_STRING
									: filter));
			} else {
			responseEvent = responseAdapter
					.invoke(JMSAdapter.REC_AND_CONVERT_SELECTED,
							BatchTransalationEvent.class, responseDestination,
							timeout > 0 ? timeout : responseAdapter
									.getResource().getReceiveTimeout(), (filter
									.length() == CORRELATION_ID_FILTER_PREFIX
									.length() + 1 ? StringHelper.EMPTY_STRING
									: filter));
			}
			if (responseEvent == null) {
				throw new BatchException("Timed out waiting for the response.");
			} else {
				String status = (String) responseEvent.getBody().get(1)
						.getValue();
				String errorMessage = null;
				if (!"SUCCESS".equalsIgnoreCase(status)) {
					errorMessage = (String) responseEvent.getBody().get(2)
							.getValue();
					throw new BatchException(
							"Error  Receieved from the transalator : "
									+ errorMessage);
				}

			}

			params = (BatchVFSParameters) vfsTasklet.getBatchParameters();
			
			if (LOGGER.isDebugEnabled()&&params!=null) {
				LOGGER.debug("the batch parms is " + params.get("vfs.target.relative.folder.path"));
			}
			if (params == null) {
				params = new BatchVFSParameters(
						new LinkedHashMap<String, String>());
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("response body" + responseEvent.getBody());
			}
			params.setFileName((String) responseEvent.getBody().get(2)
					.getValue());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("the batch parms is " + params);
			}
			vfsTasklet.getAdapter().invoke("performOperation", null,
					vfsTasklet.getOperation(), params);

			return RepeatStatus.FINISHED;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("TransformationRequestTasklet.execute() completed");
			}
		}

	}

	/**
	 * @return the timeout long
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            long
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the responseAdapter JMSAdapter
	 */
	public JMSAdapter getResponseAdapter() {
		return responseAdapter;
	}

	/**
	 * @param responseAdapter
	 *            JMSAdapter
	 */
	public void setResponseAdapter(JMSAdapter responseAdapter) {
		this.responseAdapter = responseAdapter;
	}

	/**
	 * @return the responseDestination String
	 */
	public String getResponseDestination() {
		return responseDestination;
	}

	/**
	 * @param responseDestination
	 *            String
	 */
	public void setResponseDestination(String responseDestination) {
		this.responseDestination = responseDestination;
	}

	/**
	 * @return the vfsTasklet VFSTasklet
	 */
	public VFSTasklet getVfsTasklet() {
		return vfsTasklet;
	}

	/**
	 * @param vfsTasklet
	 *            VFSTasklet
	 */
	public void setVfsTasklet(VFSTasklet vfsTasklet) {
		this.vfsTasklet = vfsTasklet;
	}

	/**
	 * Getter for responseDestinationType
	 *
	 * @return responseDestinationType String
	 */
	public String getResponseDestinationType() {
	    return responseDestinationType;
	}

	/**
	 * Setter for responseDestinationType
	 *
	 * @param responseDestinationType String
	 */
	public void setResponseDestinationType(String responseDestinationType) {
	    this.responseDestinationType = responseDestinationType;
	}

	/**
	 * Getter for responseDestinationDS
	 *
	 * @return responseDestinationDS String
	 */
	public String getResponseDestinationDS() {
	    return responseDestinationDS;
	}

	/**
	 * Setter for responseDestinationDS
	 *
	 * @param responseDestinationDS String
	 */
	public void setResponseDestinationDS(String responseDestinationDS) {
	    this.responseDestinationDS = responseDestinationDS;
	}
}
