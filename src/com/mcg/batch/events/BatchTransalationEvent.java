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
public class BatchTransalationEvent extends Event<EventElement, EventElement> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2520623559071806552L;
	public static final String BATCH_TRANSALATION_EVENT_NAME = "BatchTransalationEvent";
	public static final int BATCH_TRANSALATION_EVENT_TYPE = 3;

	public static final String TRANSALATOR_TYPE = "transalatorType";
	public static final String TRANSALATOR_REQUEST_TYPE = "requestType";
	public static final String TRANSALATOR_RESPONSE_STATUS = "status";
	public static final String TRANSALATOR_RESPONSE_TYPE = "responseType";
	public static final String TRANSALATOR_REQUEST_FUNCTION_NAME = "functionName";
	public static final String TRANSALATOR_REQUEST_PARAMETERS = "parameters";
	public static final String TRANSALATOR_REQUEST_ID = "request-id";
	public static final String TRANSALATOR_RESPONSE= "response";

	/**
	 * @param eventName
	 * @param eventType
	 */

	public BatchTransalationEvent() {
		super(BATCH_TRANSALATION_EVENT_NAME, BATCH_TRANSALATION_EVENT_TYPE);
	}

}
