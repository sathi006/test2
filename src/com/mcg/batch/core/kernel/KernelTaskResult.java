/**
 * 
 */
package com.mcg.batch.core.kernel;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class KernelTaskResult<T> {

	private T result;
	private Throwable exception;
	private boolean isResultVoid;

	/**
	 * @param resultObject
	 */
	public KernelTaskResult(T resultObject) {
		this.result = resultObject;
		isResultVoid = resultObject == null;
	}

	/**
	 * @return the result
	 */
	public T getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(T result) {
		this.result = result;
	}

	/**
	 * @return the exception
	 */
	public Throwable getException() {
		return exception;
	}

	/**
	 * @param exception
	 *            the exception to set
	 */
	public void setException(Throwable exception) {
		this.exception = exception;
	}

	@SuppressWarnings("unchecked")
	public Class<T> getReturnType() {
		return isResultVoid ? null : ((Class<T>) result.getClass());
	}

	public boolean isExeuctionSuccessful() {
		return exception == null;
	}

}
