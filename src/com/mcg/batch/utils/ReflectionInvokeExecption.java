/**
 * 
 */
package com.mcg.batch.utils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class ReflectionInvokeExecption extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5297553174640468625L;

	/**
	 * Constructor with message and a linked thorwable
	 * 
	 * @param message
	 * @param cause
	 */
	public ReflectionInvokeExecption(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with Exception Message
	 * 
	 * @param message
	 */
	public ReflectionInvokeExecption(final String message) {
		super(message);
	}

	/**
	 * Constructor with linked throwable
	 * 
	 * @param throwable
	 */
	public ReflectionInvokeExecption(final Throwable throwable) {
		super(throwable);
	}
}
