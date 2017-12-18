/**
 * 
 */
package com.mcg.batch.adapter.impl;

import static com.mcg.batch.adapters.impl.support.vfs.FSOptionsFactory.getFsOptions;
import static com.mcg.batch.adapters.impl.support.vfs.TransferType.APPEND;
import static com.mcg.batch.adapters.impl.support.vfs.TransferType.NO_REPLACE;
import static com.mcg.batch.core.BatchConfiguration.BATCH_EXECUTION_WARNINGS;
import static com.mcg.batch.core.BatchConfiguration.BATCH_FAULTY_RESOURCE;
import static com.mcg.batch.core.BatchConfiguration.FILE_COUNT_FILTER_STATUS;
import static com.mcg.batch.core.BatchConfiguration.FILE_NAME_FILTER_STATUS;
import static com.mcg.batch.core.BatchConfiguration.FILE_SIZE_FILTER_STATUS;
import static com.mcg.batch.core.BatchConfiguration.FTPS_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.FTP_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.HTTPS_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.HTTP_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.LOCAL_FS_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.NO;
import static com.mcg.batch.core.BatchConfiguration.SFTP_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.STRICT_HOST_KEY_CHECKING;
import static com.mcg.batch.utils.FileOperation.MOVE;
import static com.mcg.batch.utils.FileOperation.UNKNOWN;
import static com.mcg.batch.utils.IOHelper.KB;
import static com.mcg.batch.utils.StringHelper.SCRIPT_PARAM_DELIMITER;
import static com.mcg.batch.utils.StringHelper.UX_NEW_LINE;
import static com.mcg.batch.utils.StringHelper.WILDCARD;
import static com.mcg.batch.utils.StringHelper.concat;
import static com.mcg.batch.utils.StringHelper.isNotEmpty;
import static org.apache.commons.vfs2.FileName.SEPARATOR;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileFilterSelector;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mcg.batch.adapters.impl.support.vfs.BatchFileSelectInfo;
import com.mcg.batch.adapters.impl.support.vfs.BatchVFSParameters;
import com.mcg.batch.adapters.impl.support.vfs.FileObjectSorter;
import com.mcg.batch.adapters.impl.support.vfs.TransferType;
import com.mcg.batch.adapters.impl.support.vfs.VFSResource;
import com.mcg.batch.cache.SmartBatchCacheFactory;
import com.mcg.batch.cache.impl.RedisCacheFactory;
import com.mcg.batch.core.BatchConfiguration;
import com.mcg.batch.exceptions.AdapterException;
import com.mcg.batch.exceptions.BatchException;
import com.mcg.batch.exceptions.NonRetryableExecption;
import com.mcg.batch.exceptions.RetryableException;
import com.mcg.batch.utils.CollectionUtils;
import com.mcg.batch.utils.FileOperation;
import com.mcg.batch.utils.IOHelper;
import com.mcg.batch.utils.SortBy;
import com.mcg.batch.utils.SortOrder;
import com.mcg.batch.utils.StackUtils;
import com.mcg.batch.utils.StringHelper;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * This adapter is used to perform file operation using the <a
 * href="http://commons.apache.org/proper/commons-vfs/">Apache Commons
 * VFS(Virtual File System)</a> implementation.<br>
 * The Resource for this adapter is {@link VFSResource} and it is expected to be
 * autowired using the bean xml configuration.
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public final class VFSAdapter_Production2 extends BaseBatchAdapter<ArrayList<VFSResource>> {
	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(VFSAdapter_Production2.class);

	private static final String JSCH_EXEC = "exec";
	private static final String DISK_SPACE_COMMAND = "df -kP ";
	private static int capacity;
	private static int threshold;
	private static final SmartBatchCacheFactory CACHE_FACTORY = RedisCacheFactory
			.getInstance();

	public void performOperation(FileOperation operation,
			BatchVFSParameters batchVFSParameters)
					throws AdapterException {

		VFSResource source = resource.get(0);
		VFSResource target = resource.get(1);
		FileObject tgtFile = null;
		FileObject tgtFileclone = null;
		OutputStream os = null;
		InputStream is = null;
		StandardFileSystemManager manager = null;
		FileSystemOptions fsSourceOptions = null;
		FileSystemOptions fsTargetOptions = null;

		FileObject srcFileObject = null;
		FileObject tgtBaseFileObject = null;
		List<FileObject> sourceFiles = null;
		boolean isSourceSingleFile = true;
		boolean isSourceHTTP = HTTP_SCHEME.equals(source.getFileName()
				.getScheme())
				|| HTTPS_SCHEME.equals(source.getFileName().getScheme());
		long targetFSSize;
		long totalSourceSize;
		String scriptOutput;
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("VFSAdapter.performOperation() started");
		}
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The vfs parameters are " + batchVFSParameters);
			}
			if (operation == UNKNOWN) {
				throw new AdapterException(
						"Invalid/unknown operation specified...");
			}


			manager = new StandardFileSystemManager();
			manager.setConfiguration(BatchConfiguration.BATCH_VFS_PROVIDERS);

			manager.init();
			fsSourceOptions = getFsOptions(source, batchVFSParameters);
			fsTargetOptions = getFsOptions(target, batchVFSParameters);


			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Source Adapter URI : "
							+ source.getFileName().getFriendlyURI());
					LOGGER.debug("Source File Name Relative Path : "
							+ batchVFSParameters.getRelativeDirectory());
				}
				LOGGER.info("batchVFSParameters.getFileName() : "+batchVFSParameters.getFileName());
				LOGGER.info("batchVFSParameters.getFileName().isEmpty() : "+batchVFSParameters.getFileName().isEmpty());

				/*
				 * This if block will resolve files if the filename is present . you can see that resolve file is done by passing the directory and filename .
				 */

				if((null!=batchVFSParameters.getFileName() && !batchVFSParameters.getFileName().isEmpty())&&(!batchVFSParameters.getFileName().contains(WILDCARD))&&(!isSourceHTTP)){
					//entering fully qualified file name given scenario

					LOGGER.info("Entered the filename  not null and no wildcard condition condition");

					// resolve till file name because there are not filters

					srcFileObject = manager.resolveFile(validatenJoinFileURI(validatenJoinFileURI(source.getFileName().getURI(),
							batchVFSParameters.getRelativeDirectory()),
							batchVFSParameters.getFileName()),
							fsSourceOptions);

					LOGGER.info("the resolved file path is " + srcFileObject.getURL()  );
				}


				else if(isSourceHTTP)
				{
					//entering source is a http condition .
					LOGGER.info("entered source HTTP condition");
					srcFileObject = manager.resolveFile(source
							.getURI(concat(
									batchVFSParameters.getRelativeDirectory(),
									batchVFSParameters.getFileName())),
									fsSourceOptions);
				}

				else{

					//entering wildcard or jms-file scenario
					LOGGER.info("Entered file name given with wildcard empty or becuase of jms-file scenario ");
					LOGGER.info("batchVFSParameters.getFileName() :"+batchVFSParameters.getFileName()+"##");
					srcFileObject = manager.resolveFile(
							validatenJoinFileURI(source.getFileName().getURI(),
									batchVFSParameters.getRelativeDirectory()),
									fsSourceOptions);
					isSourceSingleFile = false;
				}


				LOGGER.info("Source URl() : "+validatenJoinFileURI(validatenJoinFileURI(source.getFileName().getURI(),
						batchVFSParameters.getRelativeDirectory()),
						batchVFSParameters.getFileName()));
				LOGGER.info("Source URl() : "+source
						.getURI(concat(
								batchVFSParameters.getRelativeDirectory(),
								batchVFSParameters.getFileName())));
				LOGGER.debug("Checking if source file exists in filesystem : "+srcFileObject.exists());






			} catch (FileSystemException e) {
				ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE,
						source.getResourceAlias());
				throw new RetryableException(
						"exception while performing operation " + operation, e);
			}
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Target Adapter URI : "
							+ target.getFileName().getFriendlyURI());
					LOGGER.debug("Target File Name Relative Path : "
							+ batchVFSParameters.getTargetRelativePath());
				}
				tgtBaseFileObject = manager.resolveFile(
						validatenJoinFileURI(target.getFileName().getURI(),
								batchVFSParameters.getTargetRelativePath()),
								fsTargetOptions);
			} catch (FileSystemException e) {
				ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE,
						target.getResourceAlias());
				throw new RetryableException(
						"exception while performing operation " + operation, e);
			}
			sourceFiles = new ArrayList<FileObject>();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The source file directory..."
						+ srcFileObject.getName().getFriendlyURI());
				LOGGER.debug("The target file directory..."
						+ tgtBaseFileObject.getName().getFriendlyURI());
			}
			LOGGER.info("isSourceSingleFile : "+isSourceSingleFile);
			if (isSourceHTTP || isSourceSingleFile) {
				sourceFiles.add(srcFileObject);
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Entering Find Files Method ######");
				}
				findFiles(srcFileObject, new FileFilterSelector(
						batchVFSParameters.getCompositeFileFilter()), true,
						sourceFiles, batchVFSParameters.getSortOrder(),
						batchVFSParameters.getSortBy());
				LOGGER.info("the resolved file name after getting into find files is " + srcFileObject.getURL());
			}
			if (batchVFSParameters.isCheckSource()) {
				updateFileFilterStatustoContext(sourceFiles, source);
			}

			StringBuilder sourceFilePaths = new StringBuilder();
			StringBuilder targetFilePaths = new StringBuilder();

			/**
			 * 1030 30 1010 Add the file names as a delimiter separated value to
			 * the file.
			 */
			if (!sourceFiles.isEmpty()) {
				sourceFilePaths.append(" \"");
				targetFilePaths.append(" \"");
				FileObject fileObject = null;

				for (int i = 0; i < sourceFiles.size(); i++) {
					fileObject = sourceFiles.get(i);
					// Check for Minimum file age...
					// For files younger than minimum file age take action as
					// configured.

					if (batchVFSParameters.getMinFileAge() > 0) {
						if (LOGGER.isInfoEnabled()) {

							LOGGER.info("For Batch Id "
									+ ThreadContextUtils
									.getJobExecutionIdAsString()
									+ " The File Age Strategy is "
									+ (batchVFSParameters.getMinFileAge() > 0));

							LOGGER.info("Minimum File Age : "
									+ batchVFSParameters.getMinFileAge());

							LOGGER.info("The source file "
									+ fileObject.getName().getPath()
									+ " was last modified at : "
									+ fileObject.getContent().getLastModifiedTime());
							LOGGER.info("current system time : "
									+ System.currentTimeMillis());

						}
						if (fileObject.getContent().getLastModifiedTime() > (System
								.currentTimeMillis() - batchVFSParameters
								.getMinFileAge())) {
							/*
							 * if (LOGGER.isInfoEnabled()) {
							 * LOGGER.info(concat(" File Name :", fileObject
							 * .getName().getPath(), NEW_LINE,
							 * " Last Modified epoch :", fileObject
							 * .getContent() .getLastModifiedTime(), NEW_LINE,
							 * "Minimum File AGE:",
							 * batchVFSParameters.getMinFileAge())); }
							 */
							switch (batchVFSParameters.getMinFileAction()) {
							case RETRY: {

								// Throw retryable exception as the files needs
								// to
								// be repopulated to get the next lastModified.
								ThreadContextUtils.addToExecutionContext(
										BATCH_FAULTY_RESOURCE,
										target.getResourceAlias());
								throw new RetryableException(
										"Retrying the File transfer as the source file "
												+ fileObject.getName()
												.getPath()
												+ " has a last modified time less than the Minimum File Age limit and the the action is retry.");
							}
							case TERMINATE: {
								/*
								 * LOGGER.error(StackUtils.formatException(
								 * ThreadContextUtils.getJobExecution().
								 * getJobInstance().getJobName(),
								 * source.getResourceAlias() ,
								 * "Terminating the Batch  as the source file "
								 * + fileObject.getName() .getPath() +
								 * " has a last modified time less than the Minimum File Age limit and the the action is terminate. "
								 * , null));
								 */
								ThreadContextUtils.addToExecutionContext(
										BATCH_FAULTY_RESOURCE,
										source.getResourceAlias());
								throw new NonRetryableExecption(
										"Terminating the Batch  as the source file "
												+ fileObject.getName()
												.getPath()
												+ " has a last modified time less than the Minimum File Age limit and the the action is terminate. ");
							}
							case SKIP: {
								if (LOGGER.isInfoEnabled()) {
									LOGGER.info("{Execution ="
											+ ThreadContextUtils
											.getJobExecutionIdAsString()
											+ " }, as per the batch configuration the file "
											+ fileObject.getName().getPath()
											+ " will be skipped as the "
											+ "last modified time less than the Minimum File Age limit");
									ThreadContextUtils
									.addToExecutionContext(
											fileObject.getName()
											.getPath(),
											"This file has been skipped for transfer as  the last modified time less than the Minimum File Age limit");
								}
								sourceFiles.remove(i);
								i--;
								continue;
							}

							}

						}
					}
					sourceFilePaths.append(fileObject.getName().getPath());
					targetFilePaths.append(tgtBaseFileObject.getName()
							.getPath()
							+ FileName.SEPARATOR_CHAR
							+ fileObject.getName().getBaseName());
					if (i != sourceFiles.size() - 1) {
						sourceFilePaths.append(SCRIPT_PARAM_DELIMITER);
						targetFilePaths.append(SCRIPT_PARAM_DELIMITER);
					}

				}
				sourceFilePaths.append("\"");
				targetFilePaths.append("\"");

			}

			if (batchVFSParameters.getSourcePreScript() != null) {

				scriptOutput = executeCommand(
						batchVFSParameters.getSourcePreScript()
						+ sourceFilePaths.toString(), source);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(concat("Executed the  source pre script ",
							batchVFSParameters.getSourcePreScript()
							+ sourceFilePaths.toString(),
							" and the output is ", scriptOutput));
				}
			}
			if (batchVFSParameters.getTargetPreScript() != null) {

				scriptOutput = executeCommand(
						batchVFSParameters.getTargetPreScript()
						+ targetFilePaths.toString(), target);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(concat("Executed the  target  pre script ",
							batchVFSParameters.getTargetPreScript()
							+ targetFilePaths.toString(),
							" and the output is ", scriptOutput));
				}
			}
			sourceFilePaths = new StringBuilder();
			targetFilePaths = new StringBuilder();
			if (!isSourceHTTP) {
				try {
					sourceFiles.clear();
					if(isSourceSingleFile){
						srcFileObject = manager.resolveFile(validatenJoinFileURI(validatenJoinFileURI(source.getFileName().getURI(),
								batchVFSParameters.getRelativeDirectory()),
								validatenJoinFileURI(SEPARATOR,batchVFSParameters.getFileName())),
								fsSourceOptions);
						sourceFiles.add(srcFileObject);

					}else{
						srcFileObject = manager.resolveFile(
								validatenJoinFileURI(source.getFileName().getURI(),
										batchVFSParameters.getRelativeDirectory()),
										fsSourceOptions);
					}
				} catch (FileSystemException e) {
					ThreadContextUtils.addToExecutionContext(
							BATCH_FAULTY_RESOURCE, source.getResourceAlias());
					throw new RetryableException(
							"exception while performing operation " + operation,
							e);
				}
				if(!isSourceSingleFile){
					findFiles(srcFileObject, new FileFilterSelector(
							batchVFSParameters.getCompositeFileFilter()), true,
							sourceFiles, batchVFSParameters.getSortOrder(),
							batchVFSParameters.getSortBy());
				}
				if (batchVFSParameters.isCheckSource()) {
					updateFileFilterStatustoContext(sourceFiles, source);
				}
			}
			targetFSSize = getFSFreeSpace(target, batchVFSParameters);
			totalSourceSize = getTotalSize(sourceFiles);

			// To check the threshold value is lesser than or equal to the
			// Filesystem used space

			if (target.getBatchVFSThreshold() > 0) {
				threshold = target.getBatchVFSThreshold();
				if (threshold <= capacity) {
					String hostname = null;
					Long jobExecutionId = ThreadContextUtils
							.getJobExecutionId();

					if (LOCAL_FS_SCHEME
							.equals(target.getFileName().getScheme())) {
						hostname = "Localhost";

					} else if (SFTP_SCHEME.equals(target.getFileName()
							.getScheme())
							|| FTP_SCHEME.equals(target.getFileName()
									.getScheme())
									|| FTPS_SCHEME.equals(target.getFileName()
											.getScheme())) {
						hostname = ((GenericFileName) target.getFileName())
								.getHostName();
					}

					String message = "The Filesystem usage has reached the threshold of "
							+ threshold + "% for the server " + hostname;

					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(message);
					}
					putWarningForExecutionId(jobExecutionId, message);

				}
			}

			if (targetFSSize < totalSourceSize) {
				/*
				 * LOGGER.error(StackUtils.formatException(ThreadContextUtils.
				 * getJobExecution(). getJobInstance().getJobName(),
				 * target.getResourceAlias() , concat(
				 * "The target Filesystem does not have enough space to perform the operation Required: "
				 * , totalSourceSize, " available: ", targetFSSize), null));
				 */
				ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE,
						target.getResourceAlias());
				throw new NonRetryableExecption(
						concat("The target Filesystem does not have enough space to perform the operation Required: ",
								totalSourceSize, " available: ", targetFSSize));
			}

			FileObject fileObject = null;
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("sourceFiles" + sourceFiles.size());
			}
			if (CollectionUtils.isNotEmpty(sourceFiles)) {
				sourceFilePaths.append(" \"");
				targetFilePaths.append(" \"");
			}
			for (int j = 0; j < sourceFiles.size(); j++) {

				fileObject = sourceFiles.get(j);
				String targetFileName = fileObject.getName().getBaseName();
				String targetFileNametemp = "";
				if (batchVFSParameters.isSingleFile()
						&& StringUtils.isNotBlank(batchVFSParameters
								.getTargetFileName())) {
					targetFileName = batchVFSParameters.getTargetFileName();
				}
				tgtFile = tgtBaseFileObject.resolveFile(validatenJoinFileURI(
						tgtBaseFileObject.getName().getPath(),
						validatenJoinFileURI(SEPARATOR , targetFileName)));
				LOGGER.debug("Target file name BASE:"
						+ fileObject.getName().getBaseName());
				LOGGER.debug("Target file name EXT:"
						+ fileObject.getName().getExtension());
				try {
					String ext = fileObject.getName().getExtension();
					if (!StringHelper.EMPTY_STRING.equals(ext)) {
						targetFileNametemp = targetFileName.substring(0,
								targetFileName.lastIndexOf(ext) - 1);
					}
				} catch (Exception e) {
					LOGGER.debug("Exception on temp target file generation");
				}
				if (tgtFile.exists()
						&& batchVFSParameters.getTransferType() == NO_REPLACE) {
					/*
					 * LOGGER.error(StackUtils.formatException(ThreadContextUtils
					 * .getJobExecution(). getJobInstance().getJobName(),
					 * target.getResourceAlias() , "The file " +
					 * tgtFile.getName().getPath() +
					 * " already exists and the batch is initiated with NO_REPLACE mode..."
					 * , null));
					 */
					ThreadContextUtils.addToExecutionContext(
							BATCH_FAULTY_RESOURCE, target.getResourceAlias());
					throw new NonRetryableExecption(
							"The file "
									+ tgtFile.getName().getPath()
									+ " already exists and the batch is initiated with NO_REPLACE mode...");
				} else if (tgtFile.exists()
						&& batchVFSParameters.getTransferType() == TransferType.COPY_OF) {
					String fileName = fileObject.getName().getBaseName();
					if (batchVFSParameters.isSingleFile()
							&& StringUtils.isNotBlank(batchVFSParameters
									.getTargetFileName())) {
						fileName = batchVFSParameters.getTargetFileName();
					}
					String ext = fileObject.getName().getExtension();
					String newFileName = fileName;
					if (!StringHelper.EMPTY_STRING.equals(ext)) {
						newFileName = fileName.substring(0,
								fileName.lastIndexOf(ext) - 1);
						ext = "." + ext;
					}
					for (int i = 1;; i++) {
						// tgtFile = manager.resolveFile(tgtBaseFileObject,
						// (newFileName + "_" + i + ext));
						tgtFile = tgtBaseFileObject
								.resolveFile(validatenJoinFileURI(
										tgtBaseFileObject.getName().getPath(),
										FileName.SEPARATOR_CHAR + newFileName
										+ "_" + i + ext));
						targetFileName = newFileName + "_" + i + ext;
						// tgtFile=tgtBaseFileObject.resolveFile(newFileName +
						// "_" + i + ext,NameScope.CHILD);
						if (!tgtFile.exists()) {
							break;
						}

					}
				}

				tgtFileclone = tgtFile;
				tgtFile = tgtBaseFileObject
						.resolveFile(validatenJoinFileURI(tgtBaseFileObject
								.getName().getPath(), FileName.SEPARATOR_CHAR
								+ targetFileNametemp + ".part"));

				if (tgtFile.exists()) {
					LOGGER.debug("part file is already exists!!!!!!!!");
					for (int i = 1;; i++) {
						tgtFile = tgtBaseFileObject
								.resolveFile(validatenJoinFileURI(
										tgtBaseFileObject.getName().getPath(),
										FileName.SEPARATOR_CHAR
										+ targetFileNametemp + "_" + i
										+ ".part"));
						if (!tgtFile.exists()) {
							break;
						}
					}

				}

				if ((batchVFSParameters.getTransferType() == APPEND && tgtFileclone
						.exists())) {
					tgtFileclone.moveTo(tgtFile);// Change the target file to
					// part file name, because
					// get content of target
					// file.
					os = tgtFile.getContent().getOutputStream(
							batchVFSParameters.getTransferType() == APPEND);
				} else {
					os = tgtFile.getContent().getOutputStream(
							batchVFSParameters.getTransferType() == APPEND
							&& tgtFile.exists());
				}
				is = fileObject.getContent().getInputStream();

				/*
				 * long transferedBytes = IOHelper.transferWithEncoding(is, os,
				 * (batchVFSParameters.getSourceCharacterSet()!=null ?
				 * batchVFSParameters.getSourceCharacterSet() : ""),
				 * (batchVFSParameters.getTargetCharacterSet()!=null ?
				 * batchVFSParameters.getTargetCharacterSet() : ""));
				 */

				long transferedBytes = IOHelper.transfer(is, os);

				os.close();// It added for SMB file transfer
				// After completion of downloading file, will goto rename the
				// actual copy of file, in between gap any batch creates same
				// file name,
				// So avoid the overwrite file, create new file name
				if (batchVFSParameters.getTransferType() == TransferType.COPY_OF) {
					tgtFileclone = tgtBaseFileObject
							.resolveFile(validatenJoinFileURI(tgtBaseFileObject
									.getName().getPath(),
									FileName.SEPARATOR_CHAR + targetFileName));

					if (tgtFileclone.exists()) {
						String fileName = fileObject.getName().getBaseName();
						if (batchVFSParameters.isSingleFile()
								&& StringUtils.isNotBlank(batchVFSParameters
										.getTargetFileName())) {
							fileName = batchVFSParameters.getTargetFileName();
						}
						String ext = fileObject.getName().getExtension();
						String newFileName = fileName;
						if (!StringHelper.EMPTY_STRING.equals(ext)) {
							newFileName = fileName.substring(0,
									fileName.lastIndexOf(ext) - 1);
							ext = "." + ext;
						}

						LOGGER.debug("COPY_OF target file is already exists!!!!!!!!");
						for (int i = 1;; i++) {
							tgtFileclone = tgtBaseFileObject
									.resolveFile(validatenJoinFileURI(
											tgtBaseFileObject.getName()
											.getPath(),
											FileName.SEPARATOR_CHAR
											+ newFileName + "_" + i
											+ ext));
							if (!tgtFileclone.exists()) {
								break;
							}
						}
					}
				}

				tgtFile.moveTo(tgtFileclone);

				tgtFile = tgtBaseFileObject.resolveFile(validatenJoinFileURI(
						tgtBaseFileObject.getName().getPath(),
						FileName.SEPARATOR_CHAR + targetFileName));

				//
				// ThreadContextUtils.addToExecutionContext(
				// concat("Source File Name:", fileObject.getName().getPath(),
				// "Target File Name:", tgtFile.getName().getPath()),
				// concat("Transfered Length:", (transferedBytes / 1024),
				// "KB", " Transfer Mode:",
				// batchVFSParameters.getTransferType().getTransferType()));
				ThreadContextUtils.addToExecutionContext(
						concat("Source File Path -", j), fileObject.getName()
						.getPath());
				ThreadContextUtils.addToExecutionContext(
						concat("Target File Path -", j), tgtFile.getName()
						.getPath());
				ThreadContextUtils.addToExecutionContext(
						concat("Transfered Length -", j),
						concat(transferedBytes, " Bytes"));
				ThreadContextUtils.addToExecutionContext(
						concat("Transfered Mode -", j), batchVFSParameters
						.getTransferType().getTransferType());
				/*
				 * LOGGER.info("Source Adapter : "+source.getResourceAlias());
				 * LOGGER.info("Target Adapter : "+target.getResourceAlias());
				 */
				ThreadContextUtils.addToExecutionContext("Source Adapter",
						source.getResourceAlias());
				ThreadContextUtils.addToExecutionContext("Target Adapter",
						target.getResourceAlias());
				sourceFilePaths.append(fileObject.getName().getPath());
				targetFilePaths.append(tgtFile.getName().getPath());
				if (j != sourceFiles.size() - 1) {
					sourceFilePaths.append(SCRIPT_PARAM_DELIMITER);
					targetFilePaths.append(SCRIPT_PARAM_DELIMITER);
				} else {
					sourceFilePaths.append("\"");
					targetFilePaths.append("\"");
				}
				IOHelper.close(is, os);
				is = null;
				os = null;

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Transfer completed ... Total bytes transfered is "
							+ transferedBytes);
				}
				if (operation == MOVE && fileObject.isWriteable()) {

					fileObject.delete();
				}

				IOHelper.close(tgtFile, fileObject);

				fileObject = null;
			}
			if (batchVFSParameters.getSourcePostScript() != null) {
				scriptOutput = executeCommand(
						batchVFSParameters.getSourcePostScript()
						+ sourceFilePaths.toString(), source);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(concat("Executed the  source post script ",
							batchVFSParameters.getSourcePostScript()
							+ sourceFilePaths.toString(),
							" and the output is ", scriptOutput));
				}
			}
			if (batchVFSParameters.getTargetPostScript() != null) {
				scriptOutput = executeCommand(
						batchVFSParameters.getTargetPostScript()
						+ targetFilePaths.toString(), target);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(concat("Executed the  target  post script ",
							batchVFSParameters.getTargetPostScript()
							+ targetFilePaths.toString(),
							" and the output is ", scriptOutput));
				}
			}

		} catch (FileSystemException e) {

			/*
			if (e.getCause() instanceof SmbAuthException
					&& StackUtils.isSmbLogonFailed(e)) {
				LOGGER.error("Throwing NonRetryableException for SmbAuthException"+ e);
				throw new NonRetryableExecption(e);
			} else {*/
			throw new RetryableException(
					"exception while performing operation " + operation, e);
			//}
		} catch (IOException e) {
			throw new RetryableException(
					"exception while performing operation " + operation, e);
		} finally {
			if (manager != null) {
				try {
					manager.close();
				} catch (Exception ex) {
					LOGGER.error(
							"Unable to close the FileSystemManager will continue without blocking it as an execption... ",
							ex);
				} finally {
					manager = null;
				}

			}
			IOHelper.close(srcFileObject, tgtBaseFileObject, tgtFile);

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("VFSAdapter.performOperation() completed");
			}
		}

	}

	private static String executeCommand(String command, VFSResource vfsResource)
			throws AdapterException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("VFSAdapter.executeScript() started and the command is "
					+ command);
		}
		JSch jsch = null;
		Session session = null;
		ChannelExec channel = null;
		InputStream in = null;
		byte[] tmp;
		StringBuilder output = new StringBuilder();
		int i = -1;
		int exitCode = -1;
		try {
			if (SFTP_SCHEME.equals(vfsResource.getFileName().getScheme())
					|| FTP_SCHEME.equals(vfsResource.getFileName().getScheme())
					|| FTPS_SCHEME
					.equals(vfsResource.getFileName().getScheme())) {
				jsch = new JSch();
				if (vfsResource.getHostKeyFile() != null) {
					jsch.addIdentity(vfsResource.getHostKeyFile());
				}
				session = jsch.getSession(vfsResource.getUserName(),
						((GenericFileName) vfsResource.getFileName())
						.getHostName());
				session.setConfig(STRICT_HOST_KEY_CHECKING, NO);
				if (vfsResource.getPassword() != null) {
					session.setPassword(vfsResource.getPassword());
				}
				session.connect();
				channel = (ChannelExec) session.openChannel(JSCH_EXEC);
				channel.setCommand(command);
				in = channel.getInputStream();
				channel.connect();
				tmp = new byte[KB];

				while (true) {
					while (in.available() > 0) {
						i = in.read(tmp, 0, KB);
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("The num bytes read is " + i);
						}
						if (i < 0) {
							break;
						} else {
							output.append(new String(tmp, 0, i));
						}
					}
					if (channel.isClosed()) {
						if (in.available() > 0) {
							continue;
						} else {
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("Breaking from read loop ");
							}
							break;
						}

					}
					try {
						Thread.sleep(1000);
					} catch (Exception ee) {
						if (LOGGER.isErrorEnabled()) {
							LOGGER.error("Exception occured while sleeping..",
									ee);
						}
					}
				}

				channel.disconnect();
				exitCode = channel.getExitStatus();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("ExitStatus after command : " + exitCode);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The Command has been executed and the output is "
							+ output.toString());
				}
				if (exitCode > 0) {
					ThreadContextUtils.addToExecutionContext(
							BATCH_FAULTY_RESOURCE,
							vfsResource.getResourceAlias());
					throw new NonRetryableExecption(concat(
							"Unable to execute the command :", command)
							+ (output.length() > 0 ? concat(". Output : ",
									output.toString()) : ""));
				}
				return output.toString();
			} else {
				ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE,
						vfsResource.getResourceAlias());
				throw new NonRetryableExecption(
						"The specified scheme does not support exeuction of remote scripts...");
			}
		} catch (JSchException e) {
			ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE,
					vfsResource.getResourceAlias());
			throw new AdapterException(e);
		} catch (IOException e) {
			ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE,
					vfsResource.getResourceAlias());
			throw new AdapterException(e);
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
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("VFSAdapter.executeScript() completed");
			}
		}

	}

	private static int getFileTypeFSUsedPercentage(File file) {
		int fileUsedPercentage = 0;
		double used = (file.getTotalSpace() - file.getFreeSpace()
				/ file.getTotalSpace()) * 100;
		fileUsedPercentage = (int) Math.round(used);
		return fileUsedPercentage;
	}

	private static long getFSFreeSpace(VFSResource vfsResource,
			BatchVFSParameters batchVFSParameters) throws AdapterException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("VFSAdapter.getFSFreeSpace() started");
		}
		long freeSpace = Long.MAX_VALUE;
		String output = null;
		String[] outputLines = null;
		String errorMessage = null;
		Long executionId = ThreadContextUtils.getJobExecutionId();
		try {
			if (LOCAL_FS_SCHEME.equals(vfsResource.getFileName().getScheme())) {

				File file = new File(vfsResource.getURI());
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The local file path is  "
							+ file.getAbsolutePath());
				}
				if (!file.exists()) {
					try {
						file.mkdir();
					} catch (Exception e) {
						ThreadContextUtils.addToExecutionContext(
								BATCH_FAULTY_RESOURCE,
								vfsResource.getResourceAlias());
						throw new NonRetryableExecption(
								"Unable to create the target folder", e);
					}
				}

				freeSpace = file.getUsableSpace();
				capacity = getFileTypeFSUsedPercentage(file);
			} else if (SFTP_SCHEME
					.equals(vfsResource.getFileName().getScheme())
					|| FTP_SCHEME.equals(vfsResource.getFileName().getScheme())
					|| FTPS_SCHEME
					.equals(vfsResource.getFileName().getScheme())) {
				output = executeCommand(
						concat(DISK_SPACE_COMMAND,
								validatenJoinFileURI(vfsResource.getFileName()
										.getPath(), batchVFSParameters
										.getTargetRelativePath())), vfsResource);
				if (isNotEmpty(output)) {
					outputLines = output.toString().split(UX_NEW_LINE);
					try {
						freeSpace = Long
								.parseLong(outputLines[1].split("\\s+")[3]);
						freeSpace *= KB;
						String capacityString = outputLines[1].split("\\s+")[4];
						capacity = Integer.valueOf(capacityString.substring(0,
								capacityString.length() - 1));

					} catch (NumberFormatException e) {
						errorMessage = "Invalid Number specified...";
						putWarningForExecutionId(executionId, errorMessage);
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("Invalid Number specified..."
									+ output.toString());
						}
					} catch (NullPointerException e) {
						errorMessage = "Invalid Format specified...";
						putWarningForExecutionId(executionId, errorMessage);
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("Invalid Format expected..."
									+ output.toString());
						}
					}
				} else {
					errorMessage = "No Response received for the command executed...";
					putWarningForExecutionId(executionId, errorMessage);
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(errorMessage);
					}
				}
			}

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("VFSAdapter.getFSFreeSpace() capacity  -> "
						+ capacity);
			}
		} catch (NonRetryableExecption e) {
			errorMessage = "Unable to execute the command df -kP on the target machine";
			putWarningForExecutionId(executionId, errorMessage);
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(errorMessage);
			}
		} catch (Exception e) {
			errorMessage = "Unable to execute the command df -kP on the target machine";
			putWarningForExecutionId(executionId, errorMessage);
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(errorMessage);
			}
		} finally {

			output = null;
			outputLines = null;

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("VFSAdapter.getFSFreeSpace() completed");
			}
		}

		return freeSpace;

	}

	private static long getTotalSize(List<FileObject> fileObjects)
			throws FileSystemException {
		long size = 0L;

		for (FileObject fileObject : fileObjects) {
			size += fileObject.getContent().getSize();
		}
		return size;
	}

	private static void traverse(BatchFileSelectInfo fileInfo,
			FileSelector selector, boolean depthwise,
			List<FileObject> selected, SortOrder sortOrder, SortBy sortBy)
					throws Exception {
		try {
			FileObject file = fileInfo.getFile();
			int index = selected.size();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Checking whether file has got Child Files : "
						+ file.getType().hasChildren());
			}
			if (file.getType().hasChildren()
					&& selector.traverseDescendents(fileInfo)) {
				int curDepth = fileInfo.getDepth();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Current Depth : " + fileInfo.getDepth());
				}
				fileInfo.setDepth(curDepth + 1);

				FileObject[] children = FileObjectSorter.sortAndGetChildren(
						file, sortOrder, sortBy);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("No of Child Files : " + children.length);
				}
				for (int i = 0; i < children.length; i++) {
					FileObject child = children[i];
					fileInfo.setFile(child);
					traverse(fileInfo, selector, depthwise, selected,
							sortOrder, sortBy);
				}

				fileInfo.setFile(file);
				fileInfo.setDepth(curDepth);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Checking whether to Include the file : #####"
						+ fileInfo.getFile().getName().getBaseName());
			}
			if (selector.includeFile(fileInfo)) {
				if (depthwise) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Adding File ####" + file.getName());
					}
					selected.add(file);
				} else {
					selected.add(index, file);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void findFiles(FileObject fileObject, FileSelector selector,
			boolean depthwise, List<FileObject> selected, SortOrder sortOrder,
			SortBy sortBy) throws FileSystemException, NonRetryableExecption {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Inside findFiles looking for "
						+ fileObject.getName().getFriendlyURI());
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("File Object Exists : " + fileObject.exists());
			}
			if (fileObject.exists()) {
				BatchFileSelectInfo info = new BatchFileSelectInfo();
				info.setBaseFolder(fileObject);
				info.setDepth(0);
				info.setFile(fileObject);
				traverse(info, selector, depthwise, selected, sortOrder, sortBy);
			}
		} catch (Exception e) {
			/*if (e.getCause() instanceof SmbAuthException
					&& StackUtils.isSmbLogonFailed(e)) {
				LOGGER.error("Throwing NonRetryableException for SmbAuthException"+ e);
				throw new NonRetryableExecption(e);
			} else {*/
			throw new FileSystemException("vfs.provider/find-files.error",
					fileObject.getName(), e);
			//}
		}
	}

	/**
	 * Put warning for execution id.
	 *
	 * @param jobExecutionId
	 *            the job execution id
	 * @param message
	 *            the message
	 */
	public static void putWarningForExecutionId(final Long jobExecutionId,
			final String message) {
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("VFSAdapter.putWarningForExecutionId() started ");
			}
			CACHE_FACTORY.getCache().appendToMapEntry(BATCH_EXECUTION_WARNINGS,
					jobExecutionId, message);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("VFSAdapter.putWarningForExecutionId() completed");
			}
		}
	}

	/**
	 * Gets the warning for execution id.
	 *
	 * @param jobExecutionId
	 *            the job execution id
	 * @return the warning for execution id
	 */
	public String getWarningForExecutionId(final Long jobExecutionId) {
		String message = null;
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(".getWarningForExecutionId() started ");
			}
			message = (String) CACHE_FACTORY.getCache().getFromMap(
					BATCH_EXECUTION_WARNINGS, jobExecutionId);

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("VFSAdapter.getWarningForExecutionId() completed");
			}
		}
		return message;
	}

	private static String validatenJoinFileURI(String URI, String relativePath)
			throws NonRetryableExecption {
		String fileURI = URI;

		/*if (URI.contains(File.separator + QUESTION_CHAR)
				|| relativePath.contains(File.separator + QUESTION_CHAR)) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Unable to proceed as file path contains invalid character '?' ");
			}
			throw new NonRetryableExecption(
					"Unable to proceed as file path contains invalid character '?' ");
		}*/
		if (URI.endsWith(SEPARATOR) && relativePath.startsWith(SEPARATOR)) {
			fileURI = URI.substring(0, fileURI.length() - 1);
		}
		else if(!URI.endsWith(SEPARATOR) && !relativePath.startsWith(SEPARATOR)) {
			fileURI = concat(URI,SEPARATOR);
		}
		return concat(fileURI, relativePath);
	}

	public void updateFileFilterStatustoContext(List<FileObject> files,
			VFSResource resource) throws RetryableException {
		boolean fileCountFilterStatus = true;
		boolean fileNameFilterStatus = true;
		boolean fileSizeFilterStatus = true;

		if (ThreadContextUtils.getExecutionContext().containsKey(
				FILE_COUNT_FILTER_STATUS)) {
			fileCountFilterStatus = (boolean) ThreadContextUtils
					.getExecutionContext().get(FILE_COUNT_FILTER_STATUS);
		}

		if (ThreadContextUtils.getExecutionContext().containsKey(
				FILE_NAME_FILTER_STATUS)) {
			fileNameFilterStatus = (boolean) ThreadContextUtils
					.getExecutionContext().get(FILE_NAME_FILTER_STATUS);
		}

		if (ThreadContextUtils.getExecutionContext().containsKey(
				FILE_SIZE_FILTER_STATUS)) {
			fileSizeFilterStatus = (boolean) ThreadContextUtils
					.getExecutionContext().get(FILE_SIZE_FILTER_STATUS);
		}

		if (files.isEmpty() && !fileNameFilterStatus) {
			ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE,
					resource.getResourceAlias());
			throw new RetryableException(
					"Unable to proceed as no files found matching the given file name pattern specified at source... Terminating the batch...");
		}
		if (files.isEmpty() && !fileCountFilterStatus) {
			ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE,
					resource.getResourceAlias());
			throw new RetryableException(
					"Unable to proceed as no files found matching the given file count filter specified at source... Terminating the batch...");
		}
		if (files.isEmpty() && !fileSizeFilterStatus) {
			ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE,
					resource.getResourceAlias());
			throw new RetryableException(
					"Unable to proceed as no files found matching the given file size filter specified at source... Terminating the batch...");
		}
		if (files.isEmpty()) {
			ThreadContextUtils.addToExecutionContext(BATCH_FAULTY_RESOURCE,
					resource.getResourceAlias());
			throw new RetryableException(
					"Unable to proceed as no files found matching the criteria specified at source... Terminating the batch...");
		}
	}
}
