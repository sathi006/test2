/**
 * 
 */
package com.mcg.batch.runtime.impl.tasklet;

import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_LOCAL_TEMP_ARCHIVE_LOCATION;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_LOCAL_TEMP_LOCATION;
import static com.mcg.batch.core.BatchConfiguration.VFS_TEMP_FILE_ACTION;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class TempFileCleanUp implements Tasklet {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TempFileCleanUp.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.
	 * springframework.batch.core.StepContribution,
	 * org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("TempFileCleanUp.execute() started");
		}

		try {
			File tempFile = new File(BATCH_VFS_LOCAL_TEMP_LOCATION,
					ThreadContextUtils.getJobInstanceIdAsString());
			switch (VFS_TEMP_FILE_ACTION) {
			case DELETE: {

				if (tempFile.exists() && tempFile.isDirectory()) {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("The temp file cleanup action is delete. Proceeding to delete "
								+ tempFile.getAbsolutePath());
					}
					FileUtils.deleteDirectory(tempFile);
				}
				break;
			}
			case MOVE: {

				tempFile = new File(BATCH_VFS_LOCAL_TEMP_LOCATION,
						ThreadContextUtils.getJobInstanceIdAsString());

				File targetDir = new File(
						BATCH_VFS_LOCAL_TEMP_ARCHIVE_LOCATION,
						ThreadContextUtils.getJobInstanceIdAsString());

				if (tempFile.exists() && !targetDir.exists()) {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("The temp file cleanup action is move. Proceeding to move ... Source directory:"
								+ tempFile.getAbsolutePath()
								+ " Target Directory:"
								+ targetDir.getAbsolutePath());
					}
					FileUtils.moveDirectory(tempFile, targetDir);
				}
				break;
			}
			case NONE: {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("The temp file cleanup action is None. Leaving the temp file untouched..."
							+ tempFile.getAbsolutePath());
				}
				break;
			}

			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("TempFileCleanUp.execute() completed");
			}
		}
		return null;
	}
}