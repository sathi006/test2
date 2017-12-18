/**
 * 
 */
package com.mcg.batch.runtime.impl.tasklet;

import static com.mcg.batch.utils.CollectionUtils.isEmpty;
import static com.mcg.batch.utils.StringHelper.COMMA_CHAR;
import static com.mcg.batch.utils.StringHelper.delimitedStringToArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.mcg.batch.core.kernel.SmartBatchKernel;
import com.mcg.batch.exceptions.BatchException;
import com.mcg.batch.utils.CollectionUtils;
import com.mcg.batch.utils.StringHelper;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * This class is to be used by batch definitions for archival Purpose.
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class ArchiverTasklet implements Tasklet {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArchiverTasklet.class);

	private static final String JOB_LIST_PARAM = "batch-list";
	private static final String RETENTION_DAYS_PARAM = "retention-period";
	private static final String DESCRIPTION_PARAM = "description";
	private static final String DESCRIPTION = "Archive initatied from scheduler triggered on ";
	private static final String FULL_ARCHIVE = "FULL";
	private static final String PARTIAL_ARCHIVE = "PARTIAL";
	private static final String ALL_BATCHES = "ALL";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.
	 * springframework.batch.core.StepContribution,
	 * org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution stepContribution,
			ChunkContext chunkContext) throws Exception {

		String jobList = null;
		ArrayList<String> batchNames = null;
		String description;
		int retentionPeriod = 0;
		String durationStr;
		long startEpoch = 0L;
		long endEpoch = System.currentTimeMillis();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ArchiverTasklet.execute() started");
		}
		try {
			jobList = (String) chunkContext.getStepContext().getJobParameters()
					.get(JOB_LIST_PARAM);
			description = (String) chunkContext.getStepContext()
					.getJobParameters().get(DESCRIPTION_PARAM);
			if (description == null) {
				description = DESCRIPTION + String.valueOf(new Date());
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The Input Job List is for archive is" + jobList);
			}
			durationStr = (String) chunkContext.getStepContext()
					.getJobParameters().get(RETENTION_DAYS_PARAM);
			if (StringHelper.isEmpty(durationStr)) {
				throw new BatchException("Batch Parameter "
						+ RETENTION_DAYS_PARAM + "  is Mandatory...");
			}
			try {
				retentionPeriod = Integer.parseInt(durationStr);
			} catch (NumberFormatException numFormatEx) {
				throw new BatchException("Invalid Batch parameter "
						+ RETENTION_DAYS_PARAM + " specified :" + durationStr);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The Duration for archive is " + retentionPeriod);
			}
			if (LOGGER.isDebugEnabled()) {
			    LOGGER.debug("Batch Names list is Non Empty : " + StringHelper.isNotEmpty(jobList));
			}
			if (StringHelper.isNotEmpty(jobList) && !ALL_BATCHES.equalsIgnoreCase(jobList)) {
				batchNames = new ArrayList<String>(
						Arrays.asList(delimitedStringToArray(jobList,
								COMMA_CHAR)));
			} else {
				batchNames = new ArrayList<String>(SmartBatchKernel
						.getInstance().getController()
						.getTotalBatchesRegistered());
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The Job List is for archive is" + batchNames.toString());
			}
			if (CollectionUtils.isNotEmpty(batchNames)) {
				endEpoch = endEpoch - (retentionPeriod * 86400000L);
				Long id = SmartBatchKernel
						.getInstance()
						.getController()
						.archive(
								batchNames,
								startEpoch,
								endEpoch,
								description,
								StringHelper.isNotEmpty(jobList) ? PARTIAL_ARCHIVE
										: FULL_ARCHIVE);
				ThreadContextUtils.addToExecutionContext("Archival Id",
						String.valueOf(id));
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("ArchiverTasklet.execute() completed");
			}
		}

		return RepeatStatus.FINISHED;
	}
}