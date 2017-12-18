/**
 * 
 */
package com.mcg.batch.exceptions;

/**
 * <p>
 * This exception is thrown by the Adapter definitions 
 * 
 * 
 * </p>
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class AdapterException extends BatchException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -80640904694230322L;

	public AdapterException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AdapterException(final String message) {
		super(message);
	}

	public AdapterException(final Throwable throwable) {
		super(throwable);
	}

}
