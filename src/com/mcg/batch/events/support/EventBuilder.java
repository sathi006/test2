package com.mcg.batch.events.support;

import static com.mcg.batch.core.BatchWiringConstants.EVENT_BUILDER_COMPONENT;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import com.mcg.batch.core.BatchConfiguration;
import com.mcg.batch.core.kernel.SmartBatchKernel;
import com.mcg.batch.events.BatchAuditEvent;
import com.mcg.batch.events.BatchExceptionEvent;
import com.mcg.batch.events.BatchExecutionEvent;
import com.mcg.batch.events.BatchNotificationEvent;
import com.mcg.batch.events.BatchTransalationEvent;
import com.mcg.batch.events.Event;
import com.mcg.batch.events.EventElement;
import com.mcg.batch.exceptions.BatchException;
import com.mcg.batch.exceptions.KernelException;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * The Class EventBuilder.
 *
 * @author jaja
 *
 *         <p>
 *         Factory methods to generate events from batch runtime objects
 */
@Component(EVENT_BUILDER_COMPONENT)
public class EventBuilder {

	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(EventBuilder.class);

	public static final String TRANSALATOR_TYPE = "transalatorType";
	public static final String TRANSALATOR_RESPONSE_STATUS = "status";
	public static final String TRANSALATOR_REQUEST_TYPE = "requestType";

	public static final String TRANSALATOR_RESPONSE_TYPE = "responseType";
	public static final String TRANSALATOR_REQUEST_FUNCTION_NAME = "functionName";
	public static final String TRANSALATOR_REQUEST_PARAMETERS = "parameters";
	public static final String TRANSALATOR_REQUEST_ID = "request-id";
	public static final String TRANSALATOR_RESPONSE = "response";

	/**
	 * Create execution event.
	 *
	 * @param jobExecution
	 *            batchInstance
	 * @return event
	 */

	public static BatchTransalationEvent createRequestEvent(
			String functionName, String requestId,
			Map<String, String> parameters) throws BatchException {
		BatchTransalationEvent event = new BatchTransalationEvent();
		JobExecution execution = null;
		try {
			execution = SmartBatchKernel.getInstance().invoke(
					"getJobExecution", JobExecution.class,
					ThreadContextUtils.getNamespace(),
					ThreadContextUtils.getJobExecutionId());
		} catch (KernelException e) {
			throw new BatchException(e);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("The Name space is :"
					+ ThreadContextUtils.getNamespace());
			LOGGER.debug("The executionid is :"
					+ ThreadContextUtils.getJobExecutionId());
			LOGGER.debug("The instance id is :"
					+ ThreadContextUtils.getJobInstanceID());
		}
		EventBuilder.addStandardHeader(event.getHeader(), execution);
		event.getBody().add(
				new EventElement(TRANSALATOR_TYPE, TRANSALATOR_REQUEST_TYPE));
		event.getBody().add(
				new EventElement(TRANSALATOR_REQUEST_FUNCTION_NAME,
						functionName));

		event.getBody()
				.add(new EventElement(TRANSALATOR_REQUEST_ID, requestId));

		event.getBody().add(
				new EventElement(TRANSALATOR_REQUEST_PARAMETERS, parameters));
		return event;
	}

	public static BatchTransalationEvent createResponseEvent(
			String message, String requestId,String status) {
		BatchTransalationEvent event = new BatchTransalationEvent();
		event.getBody().add(
				new EventElement(TRANSALATOR_TYPE, TRANSALATOR_RESPONSE_TYPE));
		event.getBody().add(
				new EventElement(TRANSALATOR_RESPONSE_STATUS, status));
		event.getBody().add(
				new EventElement(TRANSALATOR_RESPONSE,
						message));
		event.getBody()
				.add(new EventElement(TRANSALATOR_REQUEST_ID, requestId));
		return event;
	}

	public final Event<EventElement, EventElement> createExecutionEvent(
			final JobExecution jobExecution) {
		List<EventElement> header = new ArrayList<EventElement>();
		List<EventElement> body = new ArrayList<EventElement>();
		addStandardHeader(header, jobExecution);
		body.add(new EventElement("batchParameters", jobExecution
				.getJobParameters()));
		body.add(new EventElement("batchId", jobExecution.getId().toString()));
		body.add(new EventElement("batchInfoId", jobExecution.getJobInstance()
				.getId().toString()));
		body.add(new EventElement("batchInfoVersion", jobExecution
				.getJobInstance().getVersion().toString()));
		body.add(new EventElement("batchExecutionVersion", jobExecution
				.getVersion().toString()));
		body.add(new EventElement("activityName", ""));
		body.add(new EventElement("activityInstanceId", ""));
		body.add(new EventElement("activityExecutionVersion", ""));
		body.add(new EventElement("activityStatus", ""));
		body.add(new EventElement("status", jobExecution.getStatus().toString()));
		body.add(new EventElement("timeStamp", getDateTime()));
		body.add(new EventElement("data", ""));
		BatchExecutionEvent event = new BatchExecutionEvent();
		event.setHeader(header);
		event.setBody(body);
		return event;
	}

	/**
	 * Create translator execution event.
	 *
	 * @param translatorParams
	 *            Map<String,Object>
	 * @param activityInstance
	 *            StepExecution
	 * @return the event
	 */
	public final Event<EventElement, EventElement> createTranslatorExecutionEvent(
			final Map<String, Object> translatorParams,
			final StepExecution activityInstance) {
		BatchExecutionEvent batchExecutionEvent = (BatchExecutionEvent) createExecutionEvent(activityInstance);
		Set<String> translatorParamsKeys = translatorParams.keySet();
		for (String key : translatorParamsKeys) {
			batchExecutionEvent.getBody().add(
					new EventElement(key, translatorParams.get(key)));
		}
		batchExecutionEvent.getHeader().add(
				new EventElement("operation", "Translate"));
		return batchExecutionEvent;
	}

	/**
	 * Create execution event.
	 *
	 * @param activityInstance
	 *            StepExecution
	 * @param data
	 *            String
	 * @return Event
	 */
	public final Event<EventElement, EventElement> createExecutionEvent(
			final StepExecution activityInstance, final String data) {

		BatchExecutionEvent batchExecutionEvent = (BatchExecutionEvent) createExecutionEvent(activityInstance);
		batchExecutionEvent.getBody().add(new EventElement("data", data));
		return batchExecutionEvent;
	}

	/**
	 * Create execution event.
	 *
	 * @param activityInstance
	 *            StepExecution
	 * @param data
	 *            Object
	 * @return Event
	 */
	public final Event<EventElement, EventElement> createExecutionEvent(
			final StepExecution activityInstance, final Object data) {

		BatchExecutionEvent batchExecutionEvent = (BatchExecutionEvent) createExecutionEvent(activityInstance);
		batchExecutionEvent.getBody().add(new EventElement("data", data));
		return batchExecutionEvent;
	}

	/**
	 * Create execution event.
	 *
	 * @param activityInstance
	 *            activityInstance
	 * @return event
	 */
	public final Event<EventElement, EventElement> createExecutionEvent(
			final StepExecution activityInstance) {
		List<EventElement> header = new ArrayList<EventElement>();
		List<EventElement> body = new ArrayList<EventElement>();
		addStandardHeader(header, activityInstance.getJobExecution());
		body.add(new EventElement("batchId", activityInstance.getJobExecution()
				.getId().toString()));
		body.add(new EventElement("batchInfoId", activityInstance
				.getJobExecution().getJobInstance().getId().toString()));
		body.add(new EventElement("batchInfoVersion", activityInstance
				.getJobExecution().getJobInstance().getVersion().toString()));
		body.add(new EventElement("batchExecutionVersion", activityInstance
				.getJobExecution().getVersion().toString()));
		body.add(new EventElement("activityName", activityInstance
				.getStepName()));
		body.add(new EventElement("activityInstanceId", activityInstance
				.getId().toString()));
		body.add(new EventElement("activityExecutionVersion", activityInstance
				.getVersion().toString()));
		body.add(new EventElement("activityStatus", activityInstance
				.getExitStatus().getExitCode().toString()));
		body.add(new EventElement("batchStatus", activityInstance
				.getJobExecution().getStatus().toString()));
		body.add(new EventElement("timeStamp", getDateTime()));
		BatchExecutionEvent event = new BatchExecutionEvent();
		event.setHeader(header);
		event.setBody(body);
		return event;
	}

	/**
	 * Create audit event.
	 *
	 * @param source
	 *            - pass the name of the method from which the event gets
	 *            originated
	 * @param jobExecution
	 *            batchInstance
	 * @return event
	 */
	public final Event<EventElement, EventElement> createAuditEvent(
			final String source, final JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("EventBuilder.createAuditEvent() started");
		}
		List<EventElement> header = new ArrayList<EventElement>();
		List<EventElement> body = new ArrayList<EventElement>();
		try {

			addStandardHeader(header, jobExecution);
			header.add(new EventElement("manualinvokereason", jobExecution
					.getJobParameters().getString("manualinvokereason")));
			// Body Details
			body.add(new EventElement("method", source));

			body.add(new EventElement("batchInstanceId", jobExecution
					.getJobInstance().getId().toString()));
			body.add(new EventElement("batchInstanceVersion", jobExecution
					.getJobInstance().getVersion().toString()));
			body.add(new EventElement("batchExecutionId", jobExecution.getId()
					.toString()));
			body.add(new EventElement("batchExecutionVersion", jobExecution
					.getVersion().toString()));
			body.add(new EventElement("batchStatus", jobExecution.getStatus()
					.toString()));
			body.add(new EventElement("timeStamp", getDateTime()));
			BatchAuditEvent event = new BatchAuditEvent();
			event.setHeader(header);
			event.setBody(body);
			return event;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("EventBuilder.createAuditEvent() completed");
			}
		}

	}

	/**
	 * Create audit event.
	 *
	 * @param source
	 *            - pass the name of the method from which the event gets
	 *            originated
	 * @param stepExecution
	 *            activityInstance
	 * @return event
	 */
	public final Event<EventElement, EventElement> createAuditEvent(
			final String source, final StepExecution stepExecution) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("EventBuilder.createAuditEvent() started");
		}

		try {
			Event<EventElement, EventElement> event = createAuditEvent(source,
					stepExecution.getJobExecution());

			event.getBody().add(
					new EventElement("stepName", stepExecution.getStepName()));
			event.getBody().add(
					new EventElement("stepExecutionId", stepExecution.getId()
							.toString()));
			event.getBody().add(
					new EventElement("stepExecutionVersion", stepExecution
							.getVersion().toString()));
			event.getBody().add(
					new EventElement("stepStatus", stepExecution.getStatus()
							.toString()));
			return event;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("EventBuilder.createAuditEvent() completed");
			}
		}

	}

	/**
	 * Create exception event.
	 *
	 * @param jobExecution
	 *            batchInstance
	 * @param throwable
	 *            - exception incurred
	 * @return event
	 */
	public final Event<EventElement, EventElement> createExceptionEvent(
			final JobExecution jobExecution, final Throwable throwable,
			final String message) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("EventBuilder.createExceptionEvent() started");
		}
		try {
			List<EventElement> header = new ArrayList<EventElement>();
			List<EventElement> body = new ArrayList<EventElement>();
			if (jobExecution != null) {

				addStandardHeader(header, jobExecution);
				// Body

				if (jobExecution.getJobInstance() != null) {
					body.add(new EventElement("batchInstanceId", jobExecution
							.getJobInstance().getId().toString()));
				}
				body.add(new EventElement("batchExecutionId", jobExecution
						.getId().toString()));
				body.add(new EventElement("status", jobExecution.getStatus()
						.toString()));

				body.add(new EventElement("exceptionType",
						(throwable != null ? throwable.getClass()
								.getSimpleName() : "BatchException")));

				if (StringUtils.isNotBlank(message)) {
					body.add(new EventElement("message", message));
				} else if (throwable != null) {
					body.add(new EventElement("message", throwable.getClass()
							.getSimpleName()));
				} else {
					body.add(new EventElement("message",
							"Unknown Execption occured"));
				}
				body.add(new EventElement("timeStamp", getDateTime()));
			}
			BatchExceptionEvent event = new BatchExceptionEvent();
			event.setHeader(header);
			event.setBody(body);
			return event;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("EventBuilder.createExceptionEvent() completed");
			}
		}
	}

	public static final void addStandardHeader(List<EventElement> header,
			JobExecution jobExecution) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("jobExecution at standardHeader" + jobExecution);
		}
		if (jobExecution != null) {
			addStandardHeader(header, jobExecution.getJobParameters(),
					jobExecution.getJobInstance().getJobName());

		}

	}

	public static final void addStandardHeader(List<EventElement> header,
			JobParameters jobParameters, String batchName) {

		if (StringUtils.isNotBlank(jobParameters.getString("environment"))
				&& !StringUtils.equalsIgnoreCase(
						jobParameters.getString("environment"), "null")) {
			header.add(new EventElement("environment", jobParameters
					.getString("environment")));
		} else {
			header.add(new EventElement("environment",
					BatchConfiguration.BATCH_ENVIRONMENT));
		}
		if (StringUtils.isNotBlank(jobParameters.getString("domain"))
				&& !StringUtils.equalsIgnoreCase(
						jobParameters.getString("domain"), "null")) {
			header.add(new EventElement("domain", jobParameters
					.getString("domain")));
		} else {
			header.add(new EventElement("domain", " "));
		}
		header.add(new EventElement("batchName", batchName));
		if (StringUtils.isNotBlank(jobParameters.getString("launchedby"))
				&& !StringUtils.equalsIgnoreCase(
						jobParameters.getString("launchedby"), "null")) {
			header.add(new EventElement("launchedby", jobParameters
					.getString("launchedby")));
		} else {
			header.add(new EventElement("launchedby", " "));
		}
		header.add(new EventElement("source", jobParameters.getString("source")));

	}

	public final Event<EventElement, EventElement> createExceptionEvent(
			final JobExecution jobExecution, final String message) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("EventBuilder.createExceptionEvent() started");
		}
		try {
			return createExceptionEvent(jobExecution, null, message);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("EventBuilder.createExceptionEvent() completed");
			}
		}
	}

	/**
	 * Create exception event.
	 *
	 * @param batchName
	 *            batchName
	 * @param batchParameters
	 *            batchParameters
	 * @param e
	 *            - exception incurred
	 * @return event
	 */
	public final Event<EventElement, EventElement> createExceptionEvent(
			final String batchName, final JobParameters batchParameters,
			final Exception e, final String message) {
		List<EventElement> header = new ArrayList<EventElement>();
		List<EventElement> body = new ArrayList<EventElement>();
		addStandardHeader(header, batchParameters, batchName);
		body.add(new EventElement("status", "EXCEPTION CAPTURED"));
		body.add(new EventElement("exceptionType", e.getClass().getSimpleName()));
		if (StringUtils.isNotBlank(message)) {
			body.add(new EventElement("message", message));
		} else {
			body.add(new EventElement("message", e.getClass().getSimpleName()));
		}
		body.add(new EventElement("timeStamp", getDateTime()));
		BatchExceptionEvent event = new BatchExceptionEvent();
		event.setHeader(header);
		event.setBody(body);
		return event;
	}

	/**
	 * Create exception event.
	 *
	 * @param jobExecution
	 *            JobExecution
	 * @return the event
	 */
	public final Event<EventElement, EventElement> createExceptionEvent(
			final JobExecution jobExecution) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("EventBuilder.createExceptionEvent() started");
		}
		try {
			return createExceptionEvent(jobExecution, jobExecution
					.getExitStatus().getExitDescription());
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("EventBuilder.createExceptionEvent() completed");
			}
		}
	}

	
	
	public final Event<EventElement, EventElement> createNotificationEvent(final String source, JobExecution jobExecution) {
		List<EventElement> header = new ArrayList<EventElement>();
		List<EventElement> body = new ArrayList<EventElement>();
		
			addStandardHeader(header, jobExecution);
			header.add(new EventElement("manualinvokereason", jobExecution
					.getJobParameters().getString("manualinvokereason")));
			// Body Details
			body.add(new EventElement("method", source));
			body.add(new EventElement("executionId", ThreadContextUtils.getJobExecutionId()));
			getExecutionContext(jobExecution,body);
			 if(jobExecution.getStatus().toString().equals("FAILED")){	
				 getStepExceptionMessage(jobExecution,body);
			}
			body.add(new EventElement("batchStartTime",jobExecution.getStartTime()));
			body.add(new EventElement("batchEndTime", jobExecution.getEndTime()));
			body.add(new EventElement("batchStatus", jobExecution.getStatus()
					.toString()));
			body.add(new EventElement("timeStamp", getDateTime()));
			BatchNotificationEvent event = new BatchNotificationEvent();
			event.setHeader(header);
			event.setBody(body);
		return event;
	}
	
	public void getExecutionContext(JobExecution jobExecution,List<EventElement> body){
		Collection<StepExecution>  stepExecutionList= jobExecution.getStepExecutions();
		for (Iterator<StepExecution> iterator = stepExecutionList.iterator(); iterator
				.hasNext();) {
			StepExecution stepExecution = (StepExecution) iterator.next();
			Set<Map.Entry<String,Object>> stepexecutionList= stepExecution.getExecutionContext().entrySet();
			for (Iterator<Entry<String, Object>> iterator2 = stepexecutionList.iterator(); iterator2
					.hasNext();) {
				Entry<String, Object> entry = (Entry<String, Object>) iterator2
						.next();
				 body.add(new EventElement("executionContext",entry.getKey()+" = "+entry.getValue()));	
			  }	
		   } 
	}
	public void getStepExceptionMessage(JobExecution jobExecution,List<EventElement> body){
		Collection<StepExecution>  stepExecutionList= jobExecution.getStepExecutions();
		String exception;
		for (Iterator<StepExecution> iterator = stepExecutionList.iterator(); iterator
				.hasNext();body.add(new EventElement("exceptionMessage", exception))) {
			StepExecution stepExecution = (StepExecution) iterator.next();
			exception = "<![CDATA[" + stepExecution.getExitStatus().getExitDescription() + "]]>";
			
	}
	}
	/**
	 * All events to have the timestamp. at the time of generation of the events
	 *
	 * @return time
	 */
	private static String getDateTime() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(
				BatchConfiguration.TIMESTAMP_FORMAT);
		return sdf.format(date);
	}

}