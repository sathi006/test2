package com.mcg.batch.exceptions;

/**
 * 
 * Custom Exception for SmartBatch extending Exception class.
 * 
 */
public class BatchException extends Exception {

	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = -5265160459177308643L;

	/**
	 * 
	 * @param message
	 *            String
	 * @param cause
	 *            Throwable
	 */
	public BatchException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public BatchException(final String message) {
		super(message);
	}

	public BatchException(final Throwable throwable) {
		super(throwable);
	}
}
