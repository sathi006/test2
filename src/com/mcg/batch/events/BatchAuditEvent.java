package com.mcg.batch.events;


/**
 * Specialized class for Audit Event extended Event Class.
 * 
 * @author sapu
 * 
 */
public class BatchAuditEvent extends Event<EventElement, EventElement> {

	public static final String BATCH_AUDIT_EVENT_NAME = "BatchAuditEvent";
	public static final int BATCH_AUDIT_EVENT_TYPE = 0;

	/**
	 * @param eventName
	 * @param eventType
	 */
	public BatchAuditEvent() {
		super(BATCH_AUDIT_EVENT_NAME, BATCH_AUDIT_EVENT_TYPE);
	}

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1017035405315704838L;

}
