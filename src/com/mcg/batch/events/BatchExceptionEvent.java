/**
 * 
 */
package com.mcg.batch.events;


/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class BatchExceptionEvent extends Event<EventElement, EventElement> {

	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = -7981560601587636268L;
	public static final String BATCH_EXCEPTION_EVENT_NAME = "BatchExceptionEvent";
	public static final int BATCH_EXCEPTION_EVENT_TYPE = 1;

	/**
	 * 
	 */
	public BatchExceptionEvent() {
		super(BATCH_EXCEPTION_EVENT_NAME, BATCH_EXCEPTION_EVENT_TYPE);
	}
}
