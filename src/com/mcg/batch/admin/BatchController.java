/**
 * 
 */
package com.mcg.batch.admin;

import static com.mcg.batch.core.BatchConfiguration.ADAPTER_SPRING_TRANSFROM_XSL;
import static com.mcg.batch.core.BatchConfiguration.BATCH_STATUS_ALL;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_AUDIT_LOGGER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_CONTROLLER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_META_STORE_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_RT_STORE_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.EVENT_BUILDER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SINGLETON;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_EXPLORER_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_OPERATOR_COMPONENT;
import static com.mcg.batch.core.BatchWiringConstants.SMART_BATCH_REGISTRY_COMPONENT;
import static com.mcg.batch.core.support.BatchDefinitionBean.BUSINESS_FUNCTION_KEY;
import static com.mcg.batch.core.support.BatchDefinitionBean.COUNTRY_KEY;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.BATCH_JOB_SEQ;
import static com.mcg.batch.utils.IOHelper.READ_WRITE_MODE;
import static com.mcg.batch.utils.IOHelper.close;
import static com.mcg.batch.utils.IOHelper.deleteRecursive;
import static com.mcg.batch.utils.StringHelper.HYPHEN_CHAR;
import static com.mcg.batch.utils.StringHelper.concat;
import static org.springframework.batch.core.BatchStatus.COMPLETED;
import static org.springframework.batch.core.BatchStatus.FAILED;
import static org.springframework.batch.core.BatchStatus.STOPPED;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.support.PropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mcg.batch.adapter.AdapterDefinitionBean;
import com.mcg.batch.audit.BatchAuditLogger;
import com.mcg.batch.core.SmartBatchRegistry;
import com.mcg.batch.core.kernel.SmartBatchKernel;
import com.mcg.batch.core.support.BatchDefinitionBean;
import com.mcg.batch.core.support.threading.SmartBatchRuntimeContext;
import com.mcg.batch.events.Event;
import com.mcg.batch.events.support.EventBuilder;
import com.mcg.batch.exceptions.BatchException;
import com.mcg.batch.store.ArchivalInstanceDetails;
import com.mcg.batch.store.BatchMetadataStore;
import com.mcg.batch.store.BatchRuntimeStore;
import com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore;
import com.mcg.batch.utils.CollectionUtils;
import com.mcg.batch.utils.IOHelper;
import com.mcg.batch.utils.StackUtils;
import com.mcg.batch.utils.StringHelper;
import com.mcg.batch.utils.ThreadContextUtils;
import com.mcg.batch.utils.XMLUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@Component(BATCH_CONTROLLER_COMPONENT)
@Scope(SINGLETON)
public class BatchController {

	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchController.class);

	/**
	 * Key for Batch event initation.
	 */
	public static final String BATCH_EVENT_INITIATE_KEY = "batch";

	/**
	 * Key for the directory name for the batch definitions in the export
	 * archive.
	 */
	private static final String EXPORT_BATCH_DEFNS_DIR = "batch-defintions";
	/**
	 * Key for the directory name for the batch definitions in the export
	 * archive.
	 */
	private static final String EXPORT_ADAPTER_DEFNS_DIR = "adapter-defintions";

	private static final String EXPORT_ARCHIVE_RENAME_FILE = "rename-archive-batch";

	/**
	 * Prefix constant for the export archives.
	 */

	private static final String EXPORT_ARCHIVE_PREFIX = "export-";

	/**
	 * Suffix constant for the export archives.
	 */
	private static final String EXPORT_ARCHIVE_SUFFIX = ".zip";
	/**
	 * Suffix constant for the Batch Definition files.
	 */

	private static final String BATCH_DEFN_EXPORT_SUFFIX = ".dat";

	/** The Constant REGISTERED_ADAPTER_NAMES. */
	public static final String REGISTERED_ADAPTER_NAMES = "REGISTERED_ADAPTER_NAMES";

	/**
	 * smartBatchRegistry.
	 */
	@Autowired
	@Qualifier(SMART_BATCH_REGISTRY_COMPONENT)
	private SmartBatchRegistry smartBatchRegistry;

	/**
	 * batchOperator.
	 */
	@Autowired
	@Qualifier(SMART_BATCH_OPERATOR_COMPONENT)
	private JobOperator batchOperator;

	/**
	 * batchExplorer.
	 */
	@Autowired
	@Qualifier(SMART_BATCH_EXPLORER_COMPONENT)
	private JobExplorer batchExplorer;

	/**
	 * batch runtime store
	 */
	@Autowired
	@Qualifier(BATCH_RT_STORE_COMPONENT)
	BatchRuntimeStore batchStore;

	/**
	 * batch runtime store
	 */
	@Autowired
	@Qualifier(BATCH_META_STORE_COMPONENT)
	BatchMetadataStore batchMetaStore;

	/**
	 * Audit Logger
	 */
	@Autowired
	@Qualifier(BATCH_AUDIT_LOGGER_COMPONENT)
	BatchAuditLogger batchLogger;

	/**
	 * Event Builder
	 */
	@Autowired
	@Qualifier(EVENT_BUILDER_COMPONENT)
	EventBuilder eventBuilder;

	/**
	 * jobParametersConverter.
	 */
	private final JobParametersConverter jobParametersConverter = new DefaultJobParametersConverter();

	/**
	 * prevent external instantiation
	 */
	private BatchController() {
	}

	/**
	 * <p>
	 * This method creates a new instance of Batch Execution with batch name and
	 * parameters. It can be invoked from outside for batch instantiation.
	 * Suitable for web-services based invocations.
	 * 
	 * @param batchName
	 * @param parameters
	 * @return
	 * @throws BatchException
	 */

	public final Long instantiateNewBatch(final String batchName,
			final String parameters) throws BatchException {
		Event<?, ?> event = null;
		String params = null;
		Long id = null;
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.instantiateNewBatch() started ");
		}
		try {
			if (!batchMetaStore.isBatchReistered(batchName)) {
				throw new Exception(
						"Definition for "
								+ batchName
								+ " might not be available / might have been unregistered");
			}
			id = batchOperator.start(batchName, parameters);
			
			//Sathish Change To Store Batch Last Execution Date In Definition - Start
			BatchDefinitionBean batchDefBean=getBatchDefintion(batchName);
			batchDefBean.getParams().put("LASTEXECUTIONDATE", new Date());
			batchMetaStore.registerBatch(batchDefBean);
			//Sathish Change To Store Batch Last Execution Date In Definition - End
		
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(concat("An instance of batch {", batchName,
						"} has been started and the executionid is ", id));
			}
			return id;
		} catch (Exception e) {
			JobParameters jobParameters = jobParametersConverter
					.getJobParameters(PropertiesConverter
							.stringToProperties(params));
			String message = "Exception during initialization of batch : ";
			LOGGER.error(StackUtils.formatException(batchName, null, null, e,
					true));
			event = eventBuilder.createExceptionEvent(batchName, jobParameters,
					e, concat(message, StackUtils.throwableToString(e)));
			batchLogger.fatal(event);
			throw new BatchException(e);
		} finally {
			event = null;
			params = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.instantiateNewBatch() completed");
			}
		}
	}

	/**
	 * Get all batch names from cache.
	 *
	 * @return batchDefinitionBeans Map<String,String>
	 */
	public final Set<String> getAllBatchesNames() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllBatchesNames() started ");
		}
		try {
			return batchMetaStore.getAllBatchNames();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAllBatchesNames() completed");
			}
		}
	}

	/**
	 * Get the batch definition bean as per the definition id/name from store.
	 * 
	 * @param batchName
	 * @return
	 */
	public final BatchDefinitionBean getBatchDefintion(String batchName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getBatchDefintion() started ");
		}
		try {

			return batchMetaStore.getBatchDefniton(batchName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getBatchDefintion() completed");
			}
		}
	}

	/**
	 * Get the batch version based on the batchname.
	 * 
	 * @param batchName
	 * @return
	 */
	public final int getBatchVersion(final String batchName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getBatchVersion() started");
		}
		try {
			return batchMetaStore.getBatchVersion(batchName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getBatchVersion() completed");
			}
		}

	}

	/**
	 * Get all batch definitions from cache.
	 *
	 * @return batchDefinitionBeans Map<String,String>
	 */
	public final Map<String, BatchDefinitionBean> getAllBatchesDefinitions() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllBatchesDefinitions() started ");
		}
		Set<String> batchNames = null;
		Map<String, BatchDefinitionBean> batchDefinitionBeansMap = new HashMap<String, BatchDefinitionBean>();

		try {
			batchNames = batchMetaStore.getAllBatchNames();
			for (String batchName : batchNames) {
				batchDefinitionBeansMap.put(batchName,
						batchMetaStore.getBatchDefniton(batchName));
				batchName = null;
			}
			return batchDefinitionBeansMap;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAllBatchesDefinitions() completed");
			}
		}
	}

	/**
	 * 
	 * @param batchName
	 */
	public final void removeBatch(final String batchName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.removeBatch() started ");
		}
		try {
			batchMetaStore.unRegisterBatch(batchName, true);
		} finally {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.removeBatch() completed");
			}
		}
	}

	/**
	 * Unregister a batch based on the batchname.
	 * 
	 * @param batchName
	 */
	public final void unRegisterBatch(final String batchName) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.unRegisterBatch() started ");
		}
		try {
			batchMetaStore.unRegisterBatch(batchName, false);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.unRegisterBatch() completed");
			}
		}
	}

	/**
	 * Get the total batch names that were registered till date.Including the
	 * deleted ones.
	 * 
	 * @return
	 */
	public Set<String> getTotalBatchesRegistered() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getTotalBatchsRegistered() started");
		}
		try {
			return batchMetaStore.getTotalBatchesRegistered();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getTotalBatchsRegistered() completed");
			}
		}
	}

	/**
	 * Register a batch to the metadata store.
	 * 
	 * @param bean
	 */

	public void registerBatch(BatchDefinitionBean bean) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.registerBatch() started ");
		}
		try {
			batchMetaStore.registerBatch(bean);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.registerBatch() completed");
			}
		}
	}

	/**
	 * Re-Register a BatchDefinition that was unregistered before.<br>
	 * Calling this method for a batch name that is already registered will have
	 * no effect.
	 * 
	 * @param batchName
	 */
	public void reRegisterBatch(String batchName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.reRegisterBatch() started");
		}
		try {
			batchMetaStore.reRegisterBatch(batchName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.reRegisterBatch() completed");
			}
		}
	}

	/**
	 * Register new batch from xml.
	 *
	 * @param batchName
	 *            String
	 * @param jobDefinitionXML
	 *            String
	 * @param params
	 *            Map<String, String>
	 * @throws BatchException
	 *             BatchException
	 */
	public final void registerNewBatchfromXML(final String batchName,
			final String jobDefinitionXML, final String intermediateXML,
			final HashMap<String, Serializable> params, String[] parameterNames)
			throws BatchException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.registerNewBatchfromXML() started");
		}
		BatchDefinitionBean bean = null;
		try {
			bean = batchMetaStore.getBatchDefniton(batchName);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The Bean  is " + bean);
			}
			if (bean == null) {
				bean = new BatchDefinitionBean(jobDefinitionXML,
						intermediateXML, 0, batchName, params, parameterNames);
			} else {
				bean.setBatchXml(jobDefinitionXML);
				bean.setIntermediateXML(intermediateXML);
				bean.setParams(params);
			}
			bean.incrementVersion();

			registerBatch(bean);

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.registerNewBatchfromXML() completed");
			}
		}

	}

	/**
	 * Get the parameter names that can be overridden during runtime.
	 * 
	 * @param batchName
	 * @return
	 */
	public final String[] getBatchParamerterNames(final String batchName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getBatchParamerters() started");
		}
		try {
			return batchMetaStore.getBatchDefniton(batchName)
					.getParameterNames();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getBatchParamerters() completed");
			}
		}
	}

	/**
	 * Restarts the jobs execution ids if they are in failed status.
	 * 
	 * @param failedExecutionIds
	 * @return
	 */
	public final List<String> restartJobs(
			final ArrayList<String> failedExecutionIds) throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.restartJobs() started");
		}
		List<String> status = new ArrayList<String>();
		try {

			for (String failedExecutionId : failedExecutionIds) {
				status.add(restartIfFailed(failedExecutionId));
				failedExecutionId = null;
			}
			return status;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.restartJobs() completed");
			}
		}
	}

	public final List<String> suspendJobs(final ArrayList<String> executionIds)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.suspendJobs() started");
		}
		List<String> status = new ArrayList<String>();
		try {

			for (String executionId : executionIds) {

				status.add(suspendIfRunning(executionId));
			}

			return status;

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.suspendJobs() completed");
			}
		}
	}

	public final String suspendIfRunning(final String executionId)
			throws NoSuchJobExecutionException, JobExecutionNotRunningException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.suspendIfRunning() started");
		}
		Long execId = null;
		JobExecution lastExecution = null;
		JobInstance jobInstance = null;
		List<JobExecution> executions = null;

		try {
			execId = Long.parseLong(executionId);
			lastExecution = batchExplorer.getJobExecution(execId);
			jobInstance = lastExecution.getJobInstance();
			executions = batchExplorer.getJobExecutions(jobInstance);

			for (JobExecution exec : executions) {
				if (COMPLETED.equals(exec.getStatus())) {
					return concat("AC|", exec.getId().toString(), "|",
							COMPLETED.toString());
				} else if (STOPPED.equals(exec.getStatus())) {
					return concat("AS|", exec.getId().toString(), "|",
							COMPLETED.toString());
				}
				exec = null;
			}
			batchOperator.stop(execId);
			return concat("S", "|", execId);
		} finally {
			execId = null;
			lastExecution = null;
			jobInstance = null;
			executions = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.suspendIfRunning() completed");
			}
		}
	}
	
	public final String importBuild(String filePath) throws Exception {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.importBatchDefintion() started");
		}
		ZipFile zipFile = null;
		ZipEntry zipEntry = null;
		List<ZipEntry> adapterEntries = null;
		List<ZipEntry> batchDefnEntries = null;
		ZipEntry archiveBatchRename = null;
		Enumeration<ZipEntry> entries = null;
		String adapterDefnStr = null;
		InputStream in = null;
		ObjectInputStream ois = null;
		BatchDefinitionBean batchDefinitionBean = null;
		String name = null;
		StringBuilder imported = null;
		File zipFileTmp = null;
		String zipFileNameTmp = null;
		String[] delimitedNames = null;
		try {
			imported = new StringBuilder();
			zipFile = new ZipFile(filePath);
			zipFileTmp = new File(filePath);
			zipFileNameTmp = zipFileTmp.getName();
			delimitedNames = zipFileNameTmp.split("-");
			if (delimitedNames != null && delimitedNames.length >= 3) {
				ThreadContextUtils
						.setRunTimeContext(new SmartBatchRuntimeContext(
								delimitedNames[1]));
			} else {
				throw new Exception(
						"Invalid File Name: The file name is expected to be in format export-<namespace>-epoch[*].zip");
			}
			adapterEntries = new ArrayList<ZipEntry>();
			batchDefnEntries = new ArrayList<ZipEntry>();
			entries = (Enumeration<ZipEntry>) zipFile.entries();
			while (entries.hasMoreElements()) {
				zipEntry = entries.nextElement();
				if (zipEntry.getName().startsWith(EXPORT_ADAPTER_DEFNS_DIR)) {
					adapterEntries.add(zipEntry);
				} else if (zipEntry.getName()
						.startsWith(EXPORT_BATCH_DEFNS_DIR)) {
					batchDefnEntries.add(zipEntry);
				} else if (zipEntry.getName().startsWith(
						EXPORT_ARCHIVE_RENAME_FILE)) {
					archiveBatchRename = zipEntry;
				}
				zipEntry = null;
			}
			for (ZipEntry adapterEntry : adapterEntries) {
				in =  zipFile.getInputStream(adapterEntry);
				adapterDefnStr = new String(IOHelper.getBytes(in));
				close(in);
				in = null;
				name = adapterEntry.getName().substring(0,
						adapterEntry.getName().lastIndexOf('.'));
				name = name.substring(EXPORT_ADAPTER_DEFNS_DIR.length() + 1);
				upsertAdapterBean(adapterDefnStr, name);
				imported.append(adapterEntry.getName());
				imported.append(StringHelper.NEW_LINE);
				adapterEntry = null;
				adapterDefnStr = null;
			}

			for (ZipEntry batchDefnEntry : batchDefnEntries) {
				in =  zipFile.getInputStream(batchDefnEntry);
				ois = new ObjectInputStream(in);
				batchDefinitionBean = (BatchDefinitionBean) ois.readObject();
				close(ois, in);
				ois = null;
				in = null;
				String oldDefnName = batchDefinitionBean.getBatchName();
				String newDefnName = null;
				String oldBatchXML = null;
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The archiver Batch Rename file is "
							+ archiveBatchRename);
				}
				if (oldDefnName != null && oldDefnName.contains(".archive.")
						&& archiveBatchRename != null) {
					in = zipFile.getInputStream(archiveBatchRename);
					newDefnName = new String(IOHelper.getBytes(in));
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Renaming the archiver batch to "
								+ newDefnName);
					}
					close(in);
					in = null;

				} else if (oldDefnName != null
						&& oldDefnName.contains(".archive.")
						&& !(oldDefnName.startsWith(delimitedNames[1]))) {
					newDefnName = delimitedNames[1]
							+ oldDefnName.substring(oldDefnName.indexOf("."),
									oldDefnName.length());
				}
				if (newDefnName != null
						&& newDefnName.trim().length() > 0
						&& newDefnName.indexOf(".archive.") > 0
						&& newDefnName.indexOf(".archive.") < newDefnName
								.length()) {
					oldBatchXML = new String(
							batchDefinitionBean.getBatchXmlAsBytes());
					batchDefinitionBean.setBatchXml(oldBatchXML.replaceAll(
							oldDefnName, newDefnName));
					batchDefinitionBean.setBatchName(newDefnName);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("The " + oldDefnName
								+ " defintion name has been set to "
								+ newDefnName);
					}

				}

				registerBatch(batchDefinitionBean);
				imported.append(batchDefnEntry.getName());
				imported.append(StringHelper.NEW_LINE);
				batchDefnEntry = null;

			}
			return imported.toString();
		} finally {
			zipFileTmp = null;
			zipFileNameTmp = null;
			delimitedNames = null;
			ThreadContextUtils.clear();
			close(ois, in);
			ois = null;
			in = null;
			entries = null;
			adapterEntries = null;
			batchDefnEntries = null;
			zipEntry = null;

			close(zipFile);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.importBatchDefintion() completed");
			}
		}

	}





	public static void main(String[] args) {
		String str = "trade.abc";
		String repl = "fm";
		System.out
				.println(repl + str.substring(str.indexOf("."), str.length()));

	}

	public final String exportBuild(ArrayList<String> batchDefns,
			String exportPath) throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.exportBatchDefns() started");
		}
		BatchDefinitionBean batchDefinitionBean = null;
		AdapterDefinitionBean adapterDefinitionBean = null;
		File tmpDirectory = null;
		File baseDirectory = null;
		File zipFile = null;
		File batchDirectory = null;
		File adapterDirectory = null;
		File adapterTmpFile = null;
		File batchFile = null;
		RandomAccessFile adapterFile = null;
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		String[] dependentAdapters = null;
		String exportFileName = concat(ThreadContextUtils.getNamespace(),
				HYPHEN_CHAR, System.currentTimeMillis());
		try {
			baseDirectory = new File(exportPath);
			if (!baseDirectory.exists()) {
				baseDirectory.mkdir();
			}
			tmpDirectory = new File(baseDirectory, exportFileName);
			tmpDirectory.mkdir();
			batchDirectory = new File(tmpDirectory, EXPORT_BATCH_DEFNS_DIR);
			batchDirectory.mkdir();
			adapterDirectory = new File(tmpDirectory, EXPORT_ADAPTER_DEFNS_DIR);
			adapterDirectory.mkdir();
			zipFile = new File(baseDirectory, concat(EXPORT_ARCHIVE_PREFIX,
					tmpDirectory.getName(), EXPORT_ARCHIVE_SUFFIX));
			for (String batchDefn : batchDefns) {
				batchDefinitionBean = batchMetaStore
						.getBatchDefniton(batchDefn);
				batchFile = new File(batchDirectory, concat(
						batchDefinitionBean.getBatchName(), HYPHEN_CHAR,
						batchDefinitionBean.getVersion(),
						BATCH_DEFN_EXPORT_SUFFIX));
				fos = new FileOutputStream(batchFile);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(batchDefinitionBean);
				close(oos, fos);
				oos = null;
				fos = null;
				dependentAdapters = batchDefinitionBean.getdependentAdapters();
				for (String dependentAdapter : dependentAdapters) {
					adapterDefinitionBean = batchMetaStore
							.getAdapter(dependentAdapter);
					adapterTmpFile = new File(adapterDirectory, concat(
							adapterDefinitionBean.getAdapterName(), ".xml"));
					if (adapterTmpFile.exists()) {
						continue;
					}
					adapterFile = new RandomAccessFile(adapterTmpFile,
							READ_WRITE_MODE);
					adapterFile.writeBytes(adapterDefinitionBean
							.getAdapterUIDefinition());
					close(adapterFile);
					dependentAdapter = null;
					adapterDefinitionBean = null;
				}
				batchDefinitionBean = null;
			}
			IOHelper.zipDirectory(tmpDirectory, zipFile);
			return zipFile.getName();

		} finally {
			close(oos, fos);
			oos = null;
			fos = null;
			baseDirectory = null;
			if (tmpDirectory != null && tmpDirectory.exists()) {
				deleteRecursive(tmpDirectory);
			}
			tmpDirectory = null;
			adapterDefinitionBean = null;
			adapterDirectory = null;
			adapterFile = null;
			adapterTmpFile = null;
			batchDefinitionBean = null;
			batchDefns = null;
			batchFile = null;
			dependentAdapters = null;
			exportFileName = null;
			zipFile = null;

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.exportBatchDefns() completed");
			}
		}

	}

	

	/**
	 * @return the batchStore BatchRuntimeStore
	 */
	public BatchRuntimeStore getBatchStore() {
		return batchStore;
	}

	/**
	 * Restart execution with check if it has failed
	 *
	 * @param executionId
	 *            String
	 * @return message String
	 * @throws Exception
	 *             Exception
	 */
	public final String restartIfFailed(final String executionId)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.restartExecutionWithCheck() started ");
		}
		Long execId = null;
		JobExecution lastExecution = null;
		JobExecution restartedExecution = null;
		JobInstance jobInstance = null;
		List<JobExecution> executions = null;
		Long id = null;

		try {
			execId = Long.parseLong(executionId);
			lastExecution = batchExplorer.getJobExecution(execId);
			jobInstance = lastExecution.getJobInstance();
			executions = batchExplorer.getJobExecutions(jobInstance);

			for (JobExecution exec : executions) {
				if (COMPLETED.equals(exec.getStatus())) {
					return concat("AC|", exec.getId().toString(), "|",
							COMPLETED.toString());
				}
				exec = null;
			}
			if (!batchMetaStore.isBatchReistered(jobInstance.getJobName())) {
				throw new Exception(
						"Definition for "
								+ jobInstance.getJobName()
								+ " might not be available / might have been unregistered");
			}
			id = batchOperator.restart(execId);
			restartedExecution = batchExplorer.getJobExecution(id);
			return concat("R", "|", restartedExecution.getId().toString());
		} finally {
			execId = null;
			lastExecution = null;
			restartedExecution = null;
			jobInstance = null;
			executions = null;
			id = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.restartExecutionWithCheck() completed");
			}
		}
	}

	/**
	 * Sets all the started jobs to failed by this node...
	 * 
	 * @throws BatchException
	 */
	public final void setAllStartedJobsToFailedStatus() throws Exception {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.setAllStartedJobsToFailedStatus() started");
		}
		List<String> previousExecutionIds;
		JobExecution execution = null;
		JobInstance instance = null;
		Long previousExecutionId = null;
		String nameSpace = null;
		try {
			previousExecutionIds = batchStore.getNodeStartedJobIds();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Previous ExecutionIds List "
						+ previousExecutionIds);
			}
			if (previousExecutionIds != null) {
				for (String previousExecutionIdStr : previousExecutionIds) {
					// previousExecutionIdStr.substring(previousExecutionIdStr.)
					previousExecutionId = Long.parseLong(previousExecutionIdStr
							.substring(previousExecutionIdStr
									.indexOf(HYPHEN_CHAR) + 1));
					nameSpace = previousExecutionIdStr.substring(0,
							previousExecutionIdStr.indexOf(HYPHEN_CHAR));

					ThreadContextUtils
							.setRunTimeContext(new SmartBatchRuntimeContext(
									nameSpace));
					execution = batchStore.getJobExecution(previousExecutionId);
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Execution found not de-linked" + execution);
					}
					instance = batchStore.getJobInstance(execution);
					execution.setJobInstance(instance);
					boolean updateRequired = false;
					ExitStatus failedStatus = ExitStatus.FAILED;
					failedStatus.addExitDescription(new BatchException(
							"Incomplete job after server restart..."));
					failedStatus
							.addExitDescription("Incomplete job after server restart...");

					if (execution != null) {
						switch (execution.getStatus()) {

						case STARTED: {
							execution.setStatus(FAILED);
							execution.setExitStatus(ExitStatus.FAILED);
							execution.setEndTime(new Date());
							updateRequired = true;
							break;
						}
						case STARTING: {
							execution.setStatus(FAILED);
							execution.setExitStatus(failedStatus);
							execution.setEndTime(new Date());
							updateRequired = true;
							break;
						}
						case UNKNOWN: {
							execution.setStatus(FAILED);
							execution.setExitStatus(failedStatus);
							execution.setEndTime(new Date());
							updateRequired = true;
							break;
						}

						default: {
							updateRequired = false;
							break;
						}
						}
						if (updateRequired) {
							batchStore.updateJobExecution(execution);
							String restartedExecutionId = restartIfFailed(execution
									.getId().toString());
							restartedExecutionId = restartedExecutionId
									.substring(
											restartedExecutionId.indexOf('|') + 1,
											restartedExecutionId.length());
							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("Batch name "
										+ execution.getJobInstance()
												.getJobName()
										+ " failed execution id : "
										+ execution.getId().toString()
										+ " and the restarted execution id : "
										+ restartedExecutionId);
							}
						}
						batchStore.deLinkNodeStartedJobs(previousExecutionId);
						execution = null;
						instance = null;
						previousExecutionId = null;
					}
				}
			}
		} finally {
			previousExecutionIds = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.setAllStartedJobsToFailedStatus() completed");
			}
		}
	}

	/**
	 * Determine Adapter type based on the name prefix.
	 * 
	 * @param namePrefix
	 * @return
	 */
	private final String getAdapterType(String namePrefix) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAdapterType() started");
		}
		try {
			if ("jms".equalsIgnoreCase(namePrefix)
					|| "jdbc".equalsIgnoreCase(namePrefix)
					|| "rsync".equalsIgnoreCase(namePrefix)) {
				return namePrefix.toLowerCase();
			} else {
				return "vfs";
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAdapterType() completed");
			}
		}
	}

	/**
	 * Update/Insert a new adapter to the metadata store. An update would
	 * require a restart of the system to take effect...
	 * 
	 * @param adapterDefnbean
	 * @throws TransformerException
	 */
	public void upsertAdapterBean(final String adapterUIDefintion,
			final String adapterName) throws TransformerException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.registerAdapterBean() started");
		}
		String adapterdefintion = null;
		AdapterDefinitionBean adapterDefinitionBean = null;
		try {

			adapterdefintion = XMLUtils.doXSLTransform(
					ADAPTER_SPRING_TRANSFROM_XSL, adapterUIDefintion);
			adapterDefinitionBean = new AdapterDefinitionBean(adapterName,
					adapterdefintion, adapterUIDefintion,
					getAdapterType(adapterName.substring(0,
							adapterName.indexOf('.'))));
			upsertAdapterBean(adapterDefinitionBean);
		} finally {
			adapterdefintion = null;
			adapterDefinitionBean = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.registerAdapterBean() completed");
			}
		}
	}

	/**
	 * Update/Insert a new adapter to the metadata store. An update would
	 * require a restart of the system to take effect...
	 * 
	 * @param adapterDefnbean
	 */
	public void upsertAdapterBean(AdapterDefinitionBean adapterDefnbean) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.registerAdapterBean() started");
		}
		try {
			batchMetaStore.registerAdapter(adapterDefnbean);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.registerAdapterBean() completed");
			}
		}
	}

	/**
	 * Retrieve all adapter names and definitions from the metadata store.
	 * 
	 * @return
	 */
	public Map<String, AdapterDefinitionBean> getAllAdapterDefinitions() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllAdapterDefinitions() started ");
		}
		Set<String> adapterNames = null;
		Map<String, AdapterDefinitionBean> adapterDefinitionBeansMap = new HashMap<String, AdapterDefinitionBean>();

		try {
			adapterNames = batchMetaStore.getAllAdapterNames();
			for (String adapterName : adapterNames) {
				adapterDefinitionBeansMap.put(adapterName,
						batchMetaStore.getAdapter(adapterName));
				adapterName = null;
			}
			return adapterDefinitionBeansMap;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAllAdapterDefinitions() completed");
			}
		}
	}

	/**
	 * Get Adapter Names By type
	 * 
	 * @param types
	 * @return
	 */

	public Map<String, List<String>> getAdapterNamesByType(
			final ArrayList<String> types) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAdapterNamesByType() started");
		}
		Map<String, List<String>> adapterNames = new HashMap<String, List<String>>();
		Map<String, AdapterDefinitionBean> allBeans = null;
		List<String> nameList = null;
		try {
			if (types != null && types.size() > 0) {
				allBeans = getAllAdapterDefinitions();
				for (Map.Entry<String, AdapterDefinitionBean> entry : allBeans
						.entrySet()) {
					if (entry.getValue() != null) {
						nameList = adapterNames.get(entry.getValue().getType());
						if (nameList == null) {
							nameList = new ArrayList<String>();
							adapterNames.put(entry.getValue().getType(),
									nameList);
						}

						nameList.add(entry.getValue().getAdapterName());
					} else {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("No Adapter Definition Found");
						}
					}
				}
			}

			adapterNames.keySet().retainAll(types);
			return adapterNames;
		} finally {
			allBeans = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAdapterNamesByType() completed");
			}
		}

	}

	/**
	 * Retrieve all adapter names and definitions from the metadata store.
	 * 
	 * @return
	 */
	public AdapterDefinitionBean getAdapterDefinition(String adapterName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAdapterDefinition() started ");
		}

		try {
			return batchMetaStore.getAdapter(adapterName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAdapterDefinition() completed");
			}
		}
	}

	/**
	 * Retrieve all adapter names and definitions from the metadata store.
	 * 
	 * @return
	 */
	public Set<String> getAllAdapterNames() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllAdapterDefinitions() started ");
		}

		try {

			return batchMetaStore.getAllAdapterNames();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAllAdapterDefinitions() completed");
			}
		}
	}

	/**
	 * Removes the adapter identified by the name from the meta data store.
	 * Though the definition is cleared from the store the spring application
	 * context will continue to have the reference throughout the life cycle of
	 * the framework.
	 * 
	 * @param adapterName
	 */
	public void removeAdapterBean(String adapterName) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.removeAdapterBean() started");
		}
		try {
			batchMetaStore.removeAdapter(adapterName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.removeAdapterBean() completed");
			}
		}
	}

	/**
	 * Upsert a property with its key and value..
	 * 
	 * @param property
	 * @param key
	 * @param value
	 */
	public void putProperty(String property, String key, String value) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.putProperty() started");
		}
		try {

			batchMetaStore.addProperties(property, key, value);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.putProperty() completed");
			}
		}
	}

	/**
	 * Get the property value based on key
	 * 
	 * @param property
	 * @param key
	 * @return
	 */
	public String getProperty(String property, String key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getProperty() started");
		}
		try {
			return batchMetaStore.getProperty(property, key);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getProperty() completed");
			}
		}
	}

	/**
	 * Remove the property key specified.
	 * 
	 * @param Property
	 * @param key
	 */
	public void removePropertyKey(String property, String key) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.removePropertyKey() started");
		}
		try {
			batchMetaStore.removePropertyKey(property, key);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.removePropertyKey() completed");
			}
		}
	}

	/**
	 * Remove all the property keys
	 * 
	 * @param Property
	 * @param key
	 */
	public void removeAllPropertyKeys(String property) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.removeAllPropertyKeys() started");
		}
		try {
			batchMetaStore.removeAllPropertyKeys(property);

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.removeAllPropertyKeys() completed");
			}
		}
	}

	/**
	 * Get all the keys for a property.
	 * 
	 * @param property
	 * @return
	 */
	public Set<String> getPropertyKeys(String property) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getPropertyNames() started");
		}
		try {
			return batchMetaStore.getPropertyKeys(property);

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getPropertyNames() completed");
			}
		}
	}

	/**
	 * Provide the list of all property registered with store.
	 * 
	 * @param property
	 * @return
	 */
	public Set<String> getAllPropertyNames() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllPropertyNames() started");
		}
		try {
			return batchMetaStore.getAllPropertyNames();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAllPropertyNames() completed");
			}
		}
	}

	/**
	 * Get property key and values from the store
	 * 
	 * @param propertyName
	 * @return
	 */
	public HashMap<String, String> getPropertyDetails(String propertyName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getPropertyDetails() started");
		}
		try {
			return batchMetaStore.getPropertyDetails(propertyName);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getPropertyDetails() completed");
			}
		}
	}

	/**
	 * Helper method to do bootstrap activities.<br>
	 * Called by the {@link SmartBatchKernel}. It will be called once and only
	 * once per name space.
	 * 
	 * @throws Exception
	 * 
	 */
	public final void init() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BatchController.init() started");
		}
		try {
			setAllStartedJobsToFailedStatus();

		} finally {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BatchController.init() completed");
			}
		}

	}

	/**
	 * Get All the batchNames that are registered
	 * 
	 * @return
	 */
	public Set<String> getAllRegisteredBatches() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllRegisteredBatches() started");
		}
		Set<String> batchNames = null;

		String batchName = null;
		try {
			batchNames = batchMetaStore.getAllBatchNames();
			for (Iterator<String> iterator = batchNames.iterator(); iterator
					.hasNext();) {
				batchName = iterator.next();
				if (!batchMetaStore.isBatchReistered(batchName)) {
					iterator.remove();
				}
			}
			return batchNames;

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAllRegisteredBatches() completed");
			}
		}
	}

	/**
	 * Get the step Execution list by the job execution id.
	 * 
	 * @param executionId
	 * @return
	 */

	public Collection<StepExecution> getStepExecutions(final Long executionId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getStepExecutions() started");
		}
		try {
			return batchStore.getJobExecution(executionId).getStepExecutions();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getStepExecutions() completed");
			}
		}
	}

	/**
	 * Archives the runtime info based on the filter conditions specified.
	 * 
	 * @param batchNames
	 * @param startEpoch
	 * @param endEpoch
	 * @return
	 */
	public Long archive(final ArrayList<String> batchNames,
			final Long startEpoch, final Long endEpoch, String description,
			String type) throws BatchException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.archive() started");
		}

		try {
			return batchStore.archive(batchNames, startEpoch, endEpoch,
					description, type);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.archive() completed");
			}
		}

	}


	public Long restore(final String id) throws BatchException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.restore() started");
		}
		try {

			return batchStore.restoreFromArchive(getArchivalDetails(
					Long.valueOf(id)).getFileName());
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.restore() completed");
			}
		}
	}

	/**
	 * Provides the list of archival instances till date.
	 * 
	 * @return
	 */
	public final Set<Long> getArchivalInstances() {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllArchivalIds() started");
		}
		try {
			return batchStore.getArchivalInstances();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAllArchivalIds() completed");
			}
		}
	}

	public ArchivalInstanceDetails getArchivalDetails(final Long id) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getArchivalDetails() started");
		}
		try {
			return batchStore.getArchivalDetails(id);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getArchivalDetails() completed");
			}
		}
	}

	private final boolean isStartedBetween(JobExecution jobExecution,
			long startedAfter, long startedBefore) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.isStartedBetween() started");
		}
		try {

			return jobExecution.getStartTime() != null
					&& jobExecution.getStartTime().getTime() > startedAfter
					&& jobExecution.getStartTime().getTime() < startedBefore;

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.isStartedBetween() completed");
			}
		}

	}

	private final boolean hasStatus(JobExecution jobExecution, String status) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.hasStatus() started");
		}
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("the status provided is " + status);
				LOGGER.debug("The All Value from Configuration is "
						+ BATCH_STATUS_ALL);
			}
			if (BATCH_STATUS_ALL.equalsIgnoreCase(status)) {

				return true;

			} else if (StringHelper.isNotEmpty(status)) {
				return status.equalsIgnoreCase(jobExecution.getStatus()
						.toString());
			} else {
				return true;
			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.hasStatus() completed");
			}
		}

	}

	/**
	 * Validate if the jobExecution belong to a specific business function. The
	 * batch may have business function as an optional field. Hence in that
	 * cases it should return true.
	 * 
	 * @param execution
	 * @param businessFunction
	 * @param batchDefinitionBean
	 * @return
	 */
	private final boolean belongsToBusinessFn(final JobExecution execution,
			final String businessFunction,
			BatchDefinitionBean batchDefinitionBean) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.belongsToCountry() started");
		}
		try {
			if (batchDefinitionBean == null) {
				return false;
			}

			if (StringHelper.isNotEmpty(businessFunction)) {
				if (batchDefinitionBean.hasParamter(BUSINESS_FUNCTION_KEY)) {
					return batchDefinitionBean.getParam(BUSINESS_FUNCTION_KEY,
							String.class).equalsIgnoreCase(businessFunction);

				} else {
					return true;
				}

			} else {
				return true;
			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.belongsToCountry() completed");
			}
		}

	}

	/**
	 * Validate if the jobExecution belong to a specific country. The batch may
	 * have country as an optional field. Hence in that cases it should return
	 * true.
	 * 
	 * @param execution
	 * @param country
	 * @param batchDefinitionBean
	 * @return
	 */
	private final boolean belongsToCountry(final JobExecution execution,
			final String country, BatchDefinitionBean batchDefinitionBean) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.belongsToCountry() started");
		}
		try {
			if (batchDefinitionBean == null) {
				return false;
			}

			if (StringHelper.isNotEmpty(country)) {

				if (batchDefinitionBean.hasParamter(COUNTRY_KEY)) {
					return batchDefinitionBean.getParam(COUNTRY_KEY,
							String.class).equalsIgnoreCase(country);

				} else {
					return true;
				}

			} else {
				return true;
			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.belongsToCountry() completed");
			}
		}

	}

	public List<JobExecution> getExecutionsByInstanceId(String instanceId) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getExecutionsByInstance() started");
		}
		List<JobExecution> executions = null;
		List<Long> executionIds = null;
		try {

			if (StringHelper.isNotEmpty(instanceId)) {
				executionIds = batchStore.getJobExecutionsIds(Long
						.parseLong(instanceId));
				if (CollectionUtils.isNotEmpty(executionIds)) {
					executions = new ArrayList<JobExecution>();
					for (Long executionId : executionIds) {
						executions.add(batchStore.getJobExecution(executionId));
					}
				}

			}

			return executions;

		} finally {
			executionIds = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getExecutionsByInstance() completed");
			}
		}
	}

	/**
	 * Search the jobExecutions based on the search condtions specified...
	 * 
	 * @param businessFunction
	 * @param country
	 * @param batchName
	 * @param startedAfter
	 * @param startedBefore
	 * @param executionId
	 * @param batchStatus
	 * @param maxResults
	 * @return
	 */

	public List<JobExecution> searchExecutions(final String businessFunction,
			final String country, final String batchName,
			final Long startedAfter, final Long startedBefore,
			final Long executionId, final String batchStatus,
			final Integer maxResults) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.searchExecutions() started");
		}
		List<JobExecution> searchResult = null;
		JobExecution jobExecution = null;
		Set<String> batchNames = null;
		List<JobInstance> jobInstances;
		BatchDefinitionBean batchBean = null;
		try {
			searchResult = new ArrayList<JobExecution>();
			if (executionId != -1) {
				jobExecution = batchStore.getJobExecution(executionId);

			}

			if (StringHelper.isNotEmpty(batchName)) {
				batchNames = new HashSet<String>();
				batchNames.add(batchName);
			} else {
				batchNames = batchMetaStore.getTotalBatchesRegistered();
			}
			if (jobExecution != null) {
				batchBean = batchMetaStore.getBatchDefniton(jobExecution
						.getJobInstance().getJobName());
				if (batchNames.contains(jobExecution.getJobInstance()
						.getJobName())
						&& hasStatus(jobExecution, batchStatus)
						&& isStartedBetween(jobExecution, startedAfter,
								startedBefore)
						&& belongsToBusinessFn(jobExecution, businessFunction,
								batchBean)
						&& belongsToCountry(jobExecution, country, batchBean)) {
					searchResult.add(jobExecution);
				}
				batchBean = null;
			} else if (executionId == -1) {

				jobInstances = new ArrayList<JobInstance>();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Batch Names is " + batchNames);
				}
				for (String batchNametmp : batchNames) {

					jobInstances.addAll(batchStore.getJobInstances(
							batchNametmp, 0,
							(int) SmartBatchRedisRuntimeStore.getCache()
									.incrementAndGet(BATCH_JOB_SEQ, 0)));
					batchNametmp = null;
				}

				for (JobInstance jobInstance : jobInstances) {
					if (searchResult.size() >= maxResults) {
						break;
					}
					if (jobInstance != null) {
						jobExecution = batchStore
								.getLastJobExecution(jobInstance);

						if (jobExecution != null) {
							batchBean = batchMetaStore
									.getBatchDefniton(jobExecution
											.getJobInstance().getJobName());
							if (batchBean != null) {
								if (hasStatus(jobExecution, batchStatus)
										&& isStartedBetween(jobExecution,
												startedAfter, startedBefore)
										&& belongsToBusinessFn(jobExecution,
												businessFunction, batchBean)
										&& belongsToCountry(jobExecution,
												country, batchBean)) {

									searchResult.add(jobExecution);
								}
								batchBean = null;
							} else {
								LOGGER.warn("Invalid Job Execution encountered for Instance "
										+ jobInstance.getId()
										+ " during Search. No batch definition found..");
							}
						} else {
							LOGGER.warn("Invalid Job Execution encountered for Instance "
									+ jobInstance.getId() + " during Search!");
						}
					} else {
						LOGGER.warn("Invalid Job Instance encountered during Search!");
					}
				}
			}
			return searchResult;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.searchExecutions() completed");
			}
		}

	}

	/**
	 * Get Job Execution by id.
	 * 
	 * @param id
	 * @return
	 */
	public JobExecution getJobExecution(Long id) {

		return batchExplorer.getJobExecution(id);
	}

	/**
	 * Get all batch Names with last executed date
	 * 
	 *
	 * @return
	 */
	public List<String> getAllBatchNamesLastExecution() {

		return batchStore.getAllBatchNamesLastExecution();
	}

	/**
	 * Move all adapters to cache.
	 *
	 * @param mapName
	 *            the map name
	 */
	public void moveAllAdaptersToCache(String mapName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.moveAllAdaptersToCache() started");
		}

		batchMetaStore.moveEntriesToCache(mapName);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.moveAllAdaptersToCache() completed");
		}

	}

	public void moveAllAdaptersBackToDomainCache() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.moveAllAdaptersBackToDomainCache() started");
		}
		batchMetaStore.moveAllAdaptersToDomainBasedCache();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.moveAllAdaptersBackToDomainCache() completed");
		}
	}

	/**
	 * Get all Batch Display Names.
	 *
	 * @return batch Display Names
	 */
	public Map<String, String> getAllBatchDisplayNames() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllBatchDisplayNames() started");
		}
		Set<String> batchNames = batchMetaStore.getAllBatchNames();
		Map<String, String> batchDisplayNames = new HashMap<String, String>();
		if (batchNames != null) {
			for (String eachBatchName : batchNames) {
				batchDisplayNames.put(eachBatchName,
						batchMetaStore.getBatchDefniton(eachBatchName)
								.getParam("displayName", String.class));
			}
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllBatchDisplayNames() completed");
		}
		return batchDisplayNames;

	}
	
	public void moveAllStartedJobstoOtherNode(String fromNodeName,
			String toNodeName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.moveAllStartedJobstoOtherNode Started");
		}
		List<String> previousExecutionIds;
		Long previousExecutionId;
		String nameSpace;
		try {
			previousExecutionIds = batchStore
					.getNodeStartedJobIds(fromNodeName);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Execution Ids linked to Node " + fromNodeName
						+ " are : " + previousExecutionIds);
			}
			if (previousExecutionIds != null) {
				for (String previousExecutionIdStr : previousExecutionIds) {
					previousExecutionId = Long.parseLong(previousExecutionIdStr
							.substring(previousExecutionIdStr
									.indexOf(HYPHEN_CHAR) + 1));
					nameSpace = previousExecutionIdStr.substring(0,
							previousExecutionIdStr.indexOf(HYPHEN_CHAR));

					ThreadContextUtils
							.setRunTimeContext(new SmartBatchRuntimeContext(
									nameSpace));
					try {
						LOGGER.info("Moving Execution ID from Node : "
								+ fromNodeName + " to Node : " + toNodeName);
						batchStore.linkStartedJobIdtoNode(previousExecutionId,
								toNodeName);
						// batchStore.delinkStartedJobIdfromNode(previousExecutionId,
						// fromNodeName);
					} catch (Exception e) {
						LOGGER.error("Could not move Started Job Execution : "
								+ previousExecutionId + " from Node "
								+ fromNodeName + " due to Exception : ", e);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(
					"Exception while moving the started Job Execution from Node "
							+ fromNodeName + " : ", e);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.moveAllStartedJobstoOtherNode Finished");
			}
		}
	}

	/**
	 * Retrieve all batches names from adapter linked.
	 * 
	 * @return
	 */
	public void loadAllBatchesdepenAdapters() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllBatchesfrmAdapter() started ");
		}

		try {

			batchMetaStore.loadAllBatchesdepenAdapters();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAllBatchesfrmAdapter() completed");
			}
		}
	}

	/*
	 * TO get dependent abatches of an adapter
	 */
	public Set<String> getAllBatchesfrmAdapter(String adapter) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.getAllBatchesfrmAdapter() started ");
		}

		try {

			return batchMetaStore.getAllBatchesfrmAdapter(adapter);

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.getAllBatchesfrmAdapter() completed");
			}
		}
	}

	public void incrementBatchVersion(String batchName) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchController.increment batch version() started ");
		}
		try {
			BatchDefinitionBean beanDef = batchMetaStore
					.getBatchDefniton(batchName);
			LOGGER.trace(""+beanDef.getParams());
			LOGGER.trace("the old version of the batch " + beanDef.getBatchName() + "is--------->>>>> "  + String.valueOf(beanDef.getVersion()));
			beanDef.setVersion(beanDef.getVersion() + 1);
			LOGGER.trace("the new verions of the batch " + beanDef.getBatchName() +" is--------->>>>> "  + String.valueOf(beanDef.getVersion()));
			batchMetaStore.registerBatch(beanDef);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BatchController.increment batch version() completed");
			}
		}

	}


	

}
