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
public class CacheException extends BatchException {

	/**
	 * Auto generated serialVersionUID
	 */
	private static final long serialVersionUID = -4858370169446810899L;

	/**
	 * Constructor with message and a linked thorwable
	 * 
	 * @param message
	 * @param cause
	 */
	public CacheException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with Exception Message
	 * 
	 * @param message
	 */
	public CacheException(final String message) {
		super(message);
	}

	/**
	 * Constructor with linked throwable
	 * 
	 * @param throwable
	 */
	public CacheException(final Throwable throwable) {
		super(throwable);
	}
}
