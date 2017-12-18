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
public class NonRetryableExecption extends AdapterException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1699472030341895846L;

	/**
	 * Constructor with message and a linked thorwable
	 * 
	 * @param message
	 * @param cause
	 */
	public NonRetryableExecption(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with Exception Message
	 * 
	 * @param message
	 */
	public NonRetryableExecption(final String message) {
		super(message);
	}

	/**
	 * Constructor with linked throwable
	 * 
	 * @param throwable
	 */
	public NonRetryableExecption(final Throwable throwable) {
		super(throwable);
	}
}
