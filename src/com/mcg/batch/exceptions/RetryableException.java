/**
 * 
 */
package com.mcg.batch.exceptions;


/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class RetryableException extends AdapterException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3188933612173391295L;

	/**
	 * Constructor with message and a linked thorwable
	 * 
	 * @param message
	 * @param cause
	 */
	public RetryableException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with Exception Message
	 * 
	 * @param message
	 */
	public RetryableException(final String message) {
		super(message);
	}

	/**
	 * Constructor with linked throwable
	 * 
	 * @param throwable
	 */
	public RetryableException(final Throwable throwable) {
		super(throwable);
	}
}
