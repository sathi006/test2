/**
 * 
 */
package com.mcg.batch.events;

/**
 * @author BHMO
 *
 */
public class BatchNotificationEvent extends Event<EventElement, EventElement> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4507429246736052978L;
	public static final String BATCH_NOTIFICATION_EVENT_NAME = "BatchNotificationEvent";
	public static final int BATCH_NOTIFICATION_EVENT_TYPE = 4;

	/**
	 * 
	 */
	public BatchNotificationEvent() {
		super(BATCH_NOTIFICATION_EVENT_NAME, BATCH_NOTIFICATION_EVENT_TYPE);
	}
}
