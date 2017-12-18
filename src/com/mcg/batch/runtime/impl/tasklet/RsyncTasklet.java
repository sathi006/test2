/**
 * 
 */
package com.mcg.batch.runtime.impl.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.mcg.batch.adapter.impl.RsyncAdapter;
import com.mcg.batch.adapters.impl.support.rsync.BatchRsyncParameters;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class RsyncTasklet implements Tasklet {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RsyncTasklet.class);

	private RsyncAdapter rsyncAdapter;
	private BatchRsyncParameters batchRSyncParameters;

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
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RsyncTasklet.execute() started");
		}
		try {
			rsyncAdapter.invoke("performOperation", null, batchRSyncParameters);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RsyncTasklet.execute() completed");
			}
		}
		return RepeatStatus.FINISHED;
	}

	/**
	 * @param rsyncAdapter
	 *            RsyncAdapter
	 */
	public void setRsyncAdapter(RsyncAdapter rsyncAdapter) {
		this.rsyncAdapter = rsyncAdapter;
	}

	/**
	 * @param batchRSyncParameters
	 *            BatchRsyncParameters
	 */
	public void setBatchRSyncParameters(
			BatchRsyncParameters batchRSyncParameters) {
		this.batchRSyncParameters = batchRSyncParameters;
	}

}
