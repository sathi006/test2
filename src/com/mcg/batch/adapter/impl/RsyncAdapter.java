/**
 * 
 */
package com.mcg.batch.adapter.impl;

import static com.mcg.batch.core.BatchConfiguration.BATCH_FAULTY_RESOURCE;
import static com.mcg.batch.core.BatchConfiguration.NO;
import static com.mcg.batch.core.BatchConfiguration.STRICT_HOST_KEY_CHECKING;
import static com.mcg.batch.utils.IOHelper.KB;
import static com.mcg.batch.utils.StringHelper.AT_THE_RATE_CHAR;
import static com.mcg.batch.utils.StringHelper.COLON_CHAR;
import static com.mcg.batch.utils.StringHelper.EQUALS_CHAR;
import static com.mcg.batch.utils.StringHelper.QUOTE_CHAR;
import static com.mcg.batch.utils.StringHelper.concat;
import static com.mcg.batch.utils.StringHelper.isEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mcg.batch.adapters.impl.support.rsync.BatchRsyncParameters;
import com.mcg.batch.adapters.impl.support.rsync.RsyncAdapterResource;
import com.mcg.batch.core.BatchConfiguration;
import com.mcg.batch.exceptions.AdapterException;
import com.mcg.batch.exceptions.NonRetryableExecption;
import com.mcg.batch.exceptions.RetryableException;
import com.mcg.batch.utils.CollectionUtils;
import com.mcg.batch.utils.FileOperation;
import com.mcg.batch.utils.IOHelper;
import com.mcg.batch.utils.OsCommand;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class RsyncAdapter extends BaseBatchAdapter<List<RsyncAdapterResource>> {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RsyncAdapter.class);

	private static final String RSYNC_COMMAND = "rsync -p";
//	private static final String REMOVE_SOURCE = "--remove-source-files";
	private static final String INCLUDE_FILTER = "--include";
	private static final String EXCLUDE_FILTER = "--exclude";
	private static final String JSCH_EXEC = "exec";
	private static final char SEPARATOR = '/';

	public void performOperation(BatchRsyncParameters batchRsyncParameters)
			throws AdapterException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RsyncAdapter.performOperation() started");
		}
		RsyncAdapterResource source = null;
		RsyncAdapterResource target = null;
		OsCommand rsyncCommand = null;
		JSch jsch = null;
		Session session = null;
		ChannelExec channel = null;
		InputStream in = null;
		byte[] tmp;
		StringBuilder output = new StringBuilder();
		int i = -1;
		try {

			if (CollectionUtils.isEmpty(getResource())
					|| getResource().size() != 2) {
				throw new NonRetryableExecption(
						"The adapter resource is either null or not equals 2");
			}

			source = getResource().get(0);
			target = getResource().get(1);
			if (source != null && target != null) {
				rsyncCommand = new OsCommand(RSYNC_COMMAND);

				if (batchRsyncParameters.getOperation() == FileOperation.MOVE) {
					rsyncCommand.addParameters(BatchConfiguration.RSYNC_MOVE_OPTION);
				}

				if (batchRsyncParameters.getExcludeFilter() != null) {
					rsyncCommand
							.addParameters(concat(EXCLUDE_FILTER, EQUALS_CHAR,
									QUOTE_CHAR,
									batchRsyncParameters.getExcludeFilter(),
									QUOTE_CHAR));

				}

				if (batchRsyncParameters.getIncludeFilter() != null) {
					rsyncCommand
							.addParameters(concat(INCLUDE_FILTER, EQUALS_CHAR,
									QUOTE_CHAR,
									batchRsyncParameters.getIncludeFilter(),
									QUOTE_CHAR));

				}

				rsyncCommand.addParameters(concat(source.getPath(), SEPARATOR,
						batchRsyncParameters.getFileName()));
				if (isEquals(source.getHost(), target.getHost())
						&& isEquals(source.getUserName(), target.getUserName())) {
					rsyncCommand.addParameters(target.getPath());
				} else {
					rsyncCommand.addParameters(concat(target.getUserName(),
							AT_THE_RATE_CHAR, target.getHost(), COLON_CHAR,
							target.getPath()));

				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Executing Command on source machine:"
							+ rsyncCommand.get());

				}
				jsch = new JSch();
				session = jsch.getSession(source.getUserName(),
						source.getHost());
				jsch.addIdentity(source.getSshKeyFilePath());
				session.setConfig(STRICT_HOST_KEY_CHECKING, NO);
				session.connect();
				channel = (ChannelExec) session.openChannel(JSCH_EXEC);
				channel.setCommand(rsyncCommand.get());
				in = channel.getInputStream();
				channel.connect();
				tmp = new byte[KB];
				while ((i = in.read(tmp, 0, KB)) > 0) {
					output.append(new String(tmp, 0, i));
				}
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("The command is executed and the ");
				}
				if(channel.getExitStatus() > 0){
				    	/*LOGGER.error(StackUtils.formatException(ThreadContextUtils.getJobExecution().
				    		getJobInstance().getJobName(), source.getAdapterName() ,
				    		"Unabled to execute the command the error recd is "+ output.toString(),
				    		null));*/
				    	ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, source.getAdapterName());
					throw new NonRetryableExecption("Unabled to execute the command the error recd is "+ output.toString());
				}
			} else {
				throw new NonRetryableExecption(
						"Invalid source or target Adapter Resource...");
			}
		} catch (JSchException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, source.getAdapterName());
			throw new RetryableException("Unable to perform JSCH operation..",
					e);
		} catch (IOException e) {
		    ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE, source.getAdapterName());
			throw new RetryableException("Unable to perform JSCH operation..",
					e);
		} finally {
			IOHelper.close(in);
			in = null;
			if (channel != null) {
				channel.disconnect();
				channel = null;
			}

			if (session != null) {
				session.disconnect();
				session = null;
			}

			jsch = null;
			output = null;
			tmp = null;
			rsyncCommand = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RsyncAdapter.performOperation() completed");
			}
		}

	}



}
