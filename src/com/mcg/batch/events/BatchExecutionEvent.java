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
public class BatchExecutionEvent extends Event<EventElement, EventElement> {

	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = -1621483915880779188L;

	public static final String BATCH_EXECUTIION_EVENT_NAME = "BatchExecutionEvent";
	public static final int BATCH_EXECUTION_EVENT_TYPE = 2;

	/**
	 * 
	 */
	public BatchExecutionEvent() {
		super(BATCH_EXECUTIION_EVENT_NAME, BATCH_EXECUTION_EVENT_TYPE);
	}

}
