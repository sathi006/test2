package com.mcg.batch.adapter;

import static com.mcg.batch.core.BatchConfiguration.BATCH_FAULTY_RESOURCE;
import static com.mcg.batch.core.BatchWiringConstants.PROTOTYPE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;

import com.mcg.batch.utils.StackUtils;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 *
 * Class used for retrying during exceptions with preset interval and number of
 * retries.
 *
 */
@Component
@Scope(PROTOTYPE)
public class Retryer implements InitializingBean {

	/**
	 * total number of tries.
	 */
	private int retryLimit;

	/**
	 * number left.
	 */
	private transient int numberOfTriesLeft;

	/**
	 * wait interval.
	 */
	private long retryInterval;

	/**
	 * Logger Instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Retryer.class);

	/**
	 * Last Exception catched.
	 */
	private Throwable lastException;

	/**
	 * @return true if there are tries left
	 */
	public final boolean shouldRetry() {
		return numberOfTriesLeft > 0 && lastException != null;
	}

	/**
	 * This method should be called if a try fails.
	 * 
	 * @param exception
	 *            Exception if there are no more tries left
	 */
	public final void errorOccured(final Exception exception) {
		LOGGER.error("Retrying attempts " + numberOfTriesLeft
				+ " left on the Exception " + exception.getClass().toString());
		numberOfTriesLeft--;
		this.lastException = exception;
		if (!shouldRetry()) {
			this.numberOfTriesLeft = retryLimit;
			LOGGER.error(StackUtils.formatException(ThreadContextUtils.
				getJobExecution().getJobInstance().getJobName(),
				ThreadContextUtils.getExecutionContext().getString(BATCH_FAULTY_RESOURCE, ""),
				retryLimit
				+ " attempts to retry failed at " + retryInterval
				+ "ms interval for exception", exception));
			throw new RetryException(retryLimit
					+ " attempts to retry failed at " + retryInterval
					+ "ms interval", exception);
		}
		waitUntilNextTry();
	}

	/**
	 * Use this method to get the last Exception that occurred
	 * 
	 * @return
	 */
	public Throwable lastException() {
		return lastException;
	}

	/**
	 * @return time period between retries
	 */

	/**
	 * Sleeps for the duration of the defined interval.
	 */
	private void waitUntilNextTry() {
		try {
		    ThreadContextUtils.getExecutionContext().remove(BATCH_FAULTY_RESOURCE);
			Thread.sleep(retryInterval);
		} catch (InterruptedException ignored) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Retry Interrupted " + " with Exception. ", ignored);
			}
			
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.numberOfTriesLeft = retryLimit;
	}

	/**
	 * @param retryLimit
	 *            int
	 */
	public void setRetryLimit(int retryLimit) {
		this.retryLimit = retryLimit;
	}

	/**
	 * @param retryInterval
	 *            long
	 */
	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}

	/**
	 * @return the retryLimit int
	 */
	public int getRetryLimit() {
		return retryLimit;
	}

	/**
	 * @return the retryInterval long
	 */
	public long getRetryInterval() {
		return retryInterval;
	}

}
