/**
 * 
 */
package com.mcg.batch.core;

import static com.mcg.batch.core.BatchWiringConstants.BATCH_PARAM_INCREMENTOR_COMPONENT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link JobParametersIncrementer} used to ensure different
 * parameters are used during subsequent executions of a Job.
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@Component(BATCH_PARAM_INCREMENTOR_COMPONENT)
public class BatchParameterIncrementer implements JobParametersIncrementer {

	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchParameterIncrementer.class);

	/**
	 * @param parameters
	 *            JobParameters
	 * @return JobParameters
	 */
	@Override
	public final JobParameters getNext(final JobParameters parameters) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BatchParameterIncrementer.getNext() started ");
		}
		Long currentTime = System.currentTimeMillis();
		Long oldTime = null;
		try {

			if (parameters != null && !parameters.isEmpty()) {
				oldTime = Long.parseLong(parameters.getString(
						"current.TimeStamp",
						String.valueOf(System.currentTimeMillis())));
				currentTime = oldTime + 1;
				return new JobParametersBuilder(parameters).addString(
						"current.TimeStamp", String.valueOf(currentTime))
						.toJobParameters();
			}

			return new JobParametersBuilder().addString("current.TimeStamp",
					String.valueOf(currentTime)).toJobParameters();

		} finally {
			currentTime = null;
			oldTime = null;
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("BatchParameterIncrementer.getNext() completed");
			}
		}
	}
}