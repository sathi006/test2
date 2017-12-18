package com.mcg.batch.events;

import static com.mcg.batch.core.BatchConfiguration.BATCH_FAULTY_RESOURCE;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_AUDIT_EVENT_EMITTER_COMPONNET;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_AUDIT_LOGGER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.EVENT_BUILDER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.PROTOTYPE;
import static com.mcg.batch.utils.StringHelper.concat;

import java.util.List;

import javax.batch.runtime.BatchStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mcg.batch.audit.BatchAuditLogger;
import com.mcg.batch.events.support.EventBuilder;
import com.mcg.batch.utils.StackUtils;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * Event emitter implementation for the batch processing system. all core
 * listeners are of this type. Emits 3 types of events, BatchExecution,
 * BatchAudit and BatchException. Listens to all batch execution activities,
 * captures information and sends to the adapter.
 */

@SuppressWarnings("rawtypes")
@Component(BATCH_AUDIT_EVENT_EMITTER_COMPONNET)
@Scope(PROTOTYPE)
public class BatchAuditEventEmitter implements EventEmitter {

	/**
	 * prevent external instantiation
	 */
	private BatchAuditEventEmitter() {
	}

	/**
	 * Event Builder.
	 */
	@Autowired
	@Qualifier(EVENT_BUILDER_COMPONENT)
	private EventBuilder eventBuilder;

	/**
	 * Batch Logger.
	 */
	@Autowired
	@Qualifier(BATCH_AUDIT_LOGGER_COMPONENT)
	private BatchAuditLogger batchLogger;
	/**
	 * logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchAuditEventEmitter.class);
	/**
	 * Batch Instance.
	 */
	private JobExecution batchInstance;

	/**
	 *
	 * @return JobExecution
	 */
	public final JobExecution getBatchInstance() {
		return batchInstance;
	}

	/**
	 *
	 * @param batchInstanc
	 *            JobExecution
	 */
	public final void setBatchInstance(final JobExecution batchInstanc) {
		this.batchInstance = batchInstanc;
	}

	/**
	 * @param batchInstanc
	 *            JobExecution
	 */
	@Override
	public final void afterJob(final JobExecution batchInstanc) {
		Event<EventElement, EventElement> event = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchEventEmitter.afterJob() started ");
		}
		try {
			event = eventBuilder.createAuditEvent(
					"BatchEventEmitter.afterJob()", batchInstanc);
			batchLogger.info(event,batchInstanc);
			
			event = eventBuilder.createNotificationEvent(
					"BatchEventEmitter.afterJob()", batchInstanc);
			batchLogger.info(event,batchInstanc);
			if (ExitStatus.FAILED.compareTo(batchInstanc.getExitStatus()) == 0
					|| BatchStatus.FAILED.equals(batchInstanc.getStatus())) {
				String exceptionMsg = null;
				List<StepExecution> stepExecutions = (List<StepExecution>) batchInstanc
						.getStepExecutions();
				for (StepExecution s : stepExecutions) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("BatchEventEmitter.afterJob()"
								+ s.getExitStatus() + s.getStatus());
					}

					if (ExitStatus.FAILED.compareTo(s.getExitStatus()) == 0
							|| BatchStatus.FAILED.equals(s.getStatus())) {

						exceptionMsg = s.getExitStatus().getExitDescription();
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("BatchEventEmitter.afterJob() Breaking as the step status is failed");
						}
						break;
					}
				}
				event = eventBuilder.createExceptionEvent(batchInstanc,
						exceptionMsg);
				batchLogger.fatal(event);
				
			} else if (batchInstanc.getExitStatus().compareTo(
					ExitStatus.UNKNOWN) == 0
					|| batchInstanc.getStatus().equals(BatchStatus.STARTED)
					|| batchInstanc.getStatus().equals("UNKNOWN")) {
				String exceptionMsg = null;
				List<StepExecution> stepExecutions = (List<StepExecution>) batchInstanc
						.getStepExecutions();
				for (StepExecution s : stepExecutions) {

					if (s.getExitStatus().compareTo(ExitStatus.FAILED) == 0
							|| s.getStatus().equals(BatchStatus.FAILED)) {
						exceptionMsg = s.getExitStatus().getExitDescription();
						break;
					}
				}
				event = eventBuilder.createExceptionEvent(batchInstanc,
						exceptionMsg);
				batchLogger.fatal(event);
				
				
			} 
		} catch (Exception e) {
		       
			LOGGER.error("Exception while logging after Job Event. ", e);
			
		} finally {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("BatchEventEmitter.afterJob() completed");
			}

		}
	}

	/**
	 * @param batchInstanc
	 *            JobExecution
	 */
	@Override
	public final void beforeJob(final JobExecution batchInstanc) {
		this.setBatchInstance(batchInstanc);
		Event<EventElement, EventElement> event = eventBuilder
				.createAuditEvent("BatchEventEmitter.beforeJob()", batchInstanc);
		try {
			batchLogger.info(event, batchInstanc);
			event = eventBuilder.createNotificationEvent(
					"BatchEventEmitter.beforeJob()", batchInstanc);
			batchLogger.info(event,batchInstanc);
		} catch (Exception e) {
			LOGGER.error("Exception while logging before Job Event. ", e);
		}
	}

	/**
	 * @param activityInstance
	 *            StepExecution
	 * @return ExitStatus
	 */
	@Override
	public final ExitStatus afterStep(final StepExecution activityInstance) {

		Event<EventElement, EventElement> event = eventBuilder
				.createAuditEvent("BatchEventEmitter.afterStep()",
						activityInstance);
		try {
			batchLogger.info(event,activityInstance.getJobExecution());
			event = eventBuilder.createNotificationEvent(
					"BatchEventEmitter.afterStep()", activityInstance.getJobExecution());
			batchLogger.info(event,activityInstance.getJobExecution());		
		} catch (Exception e) {
			LOGGER.error("Exception while logging after Step Event. ", e);
		}

		return null;
	}

	/**
	 * @param activityInstance
	 *            StepExecution
	 */
	@Override
	public final void beforeStep(final StepExecution activityInstance) {

		Event<EventElement, EventElement> event = eventBuilder
				.createAuditEvent("BatchEventEmitter.beforeStep()",
						activityInstance);
		try {
			batchLogger.info(event,activityInstance.getJobExecution());
			event = eventBuilder.createNotificationEvent(
					"BatchEventEmitter.afterStep()", activityInstance.getJobExecution());
			batchLogger.info(event,activityInstance.getJobExecution());
		} catch (Exception e) {
			LOGGER.error("Exception while logging before Step Event. ", e);
		}

	}

	/**
	 * @param data
	 *            Object
	 */
	@Override
	public final void afterRead(final Object data) {
		LOGGER.debug("BatchEventEmitter.afterRead()" + " method is called");
	}

	/**
	 * Read Method.
	 */
	@Override
	public final void beforeRead() {
		LOGGER.debug("BatchEventEmitter.beforeRead()" + " method is called");
	}

	/**
	 * @param e
	 *            Exception
	 */
	@Override
	public final void onReadError(final Exception e) {
		/*String msg = concat("Error occured while reading the file ",
				throwableToString(e));*/
		LOGGER.error(StackUtils.formatException(ThreadContextUtils.
			getJobExecution().getJobInstance().getJobName(),
			ThreadContextUtils.getExecutionContext().getString(BATCH_FAULTY_RESOURCE, ""),
			"Error occured while reading the file : ", e));
//		Event<EventElement, EventElement> event = eventBuilder
//				.createExceptionEvent(this.batchInstance, e, msg);
//		try {
//			batchLogger.fatal(event);
//		} catch (Exception eventException) {
//			LOGGER.error("Exception while logging on Read Error Event. "
//					+ throwableToString(eventException));
//		}
	}

	/**
	 * @param data
	 *            List
	 */
	@Override
	public final void afterWrite(final List data) {
		LOGGER.debug("Write operation completed for" + " the data" + data);
	}

	/**
	 * @param data
	 *            List
	 */
	@Override
	public final void beforeWrite(final List data) {
		LOGGER.debug("Write operation started for the data" + data);
	}

	/**
	 * @param e
	 *            Exception
	 * @param arg1
	 *            List
	 */
	@Override
	public final void onWriteError(final Exception e, final List arg1) {
	    	LOGGER.error(StackUtils.formatException(ThreadContextUtils.
			getJobExecution().getJobInstance().getJobName(),
			ThreadContextUtils.getExecutionContext().getString(BATCH_FAULTY_RESOURCE, ""),
			"Error occured while writing the list " + arg1 + " to the file : ", e));
	    	/*String msg = concat("Error occured while writingthe file on the list ",
				arg1, " : ", throwableToString(e));
		LOGGER.error(msg);*/
//		Event<EventElement, EventElement> event = eventBuilder
//				.createExceptionEvent(this.batchInstance, e, msg);
//		try {
//			batchLogger.fatal(event);
//		} catch (Exception eventException) {
//			LOGGER.error("Exception while logging on Write Error Event. ",
//					eventException);
//		}
	}

	/**
	 * @param data
	 *            Object
	 * @param arg1
	 *            Object
	 */
	@Override
	public final void afterProcess(final Object data, final Object arg1) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(concat("processing operation completed for the data ",
					data, " with the object processed ", arg1));
		}
	}

	/**
	 * @param data
	 *            Object
	 */
	@Override
	public final void beforeProcess(final Object data) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(concat("processing operation started for the data ",
					data));
		}
	}

	/**
	 * @param data
	 *            Object
	 * @param e
	 *            Exception
	 */
	@Override
	public final void onProcessError(final Object data, final Exception e) {
	    	LOGGER.error(StackUtils.formatException(ThreadContextUtils.
			getJobExecution().getJobInstance().getJobName(),
			ThreadContextUtils.getExecutionContext().getString(BATCH_FAULTY_RESOURCE, ""),
			concat("Error occured while processing the file on the data ", data,
				" in the method BatchEventEmitter.onProcessError() : "), e));
	    	/*String msg = concat(
				"Error occured while processing the file on the data ", data,
				" in the method BatchEventEmitter.onProcessError() : ",
				throwableToString(e));
		LOGGER.error(msg);*/
//		Event<EventElement, EventElement> event = eventBuilder
//				.createExceptionEvent(this.batchInstance, e, msg);
//		try {
//			batchLogger.fatal(event);
//		} catch (Exception eventException) {
//			LOGGER.error("Exception while logging on Process Error Event. ",
//					eventException);
//		}
	}

}