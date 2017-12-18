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
public class KernelException extends BatchException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -768306032101742262L;

	/**
	 * Constructor with message and a linked thorwable
	 * 
	 * @param message
	 * @param cause
	 */
	public KernelException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with Exception Message
	 * 
	 * @param message
	 */
	public KernelException(final String message) {
		super(message);
	}

	/**
	 * Constructor with linked throwable
	 * 
	 * @param throwable
	 */
	public KernelException(final Throwable throwable) {
		super(throwable);
	}

}
