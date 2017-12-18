/**
 * 
 */
package com.mcg.batch.store.impl.redis;

import static com.mcg.batch.core.BatchConfiguration.BATCH_RESTARTABLE_AGE;
import static com.mcg.batch.core.BatchWiringConstants.BATCH_META_STORE_COMPONENT;
import static com.mcg.batch.store.ArchivalInstanceDetails.ARCHIVER_DETAILS;
import static com.mcg.batch.store.ArchivalInstanceDetails.ARCHIVE_ACTION;
import static com.mcg.batch.store.ArchivalInstanceDetails.RESTORE_ACTION;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.BATCH_JOB_EXECUTION;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.BATCH_JOB_EXECUTION_CONTEXT;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.BATCH_JOB_EXECUTION_PARAMS;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.BATCH_JOB_INSTANCE;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.BATCH_JOB_SEQ;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.BATCH_STEP_EXECUTION;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.BATCH_STEP_EXECUTION_CONTEXT;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.JOB_EXEC_INSTANCE_ID_PREFIX;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.JOB_ID_STEP_ID_LIST_PREFIX;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.JOB_INSTANCE_EXEC_ID_LIST_PREFIX;
import static com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore.JOB_NAME_ISTANCE_ID_LIST_PREFIX;
import static com.mcg.batch.utils.CollectionUtils.isEmpty;
import static com.mcg.batch.utils.IOHelper.close;
import static com.mcg.batch.utils.StringHelper.concat;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;

import com.mcg.batch.cache.SmartBatchCacheFactory;
import com.mcg.batch.cache.impl.RedisCacheFactory;
import com.mcg.batch.core.BatchConfiguration;
import com.mcg.batch.core.support.BatchDefinitionBean;
import com.mcg.batch.core.support.threading.SmartBatchRuntimeContext;
import com.mcg.batch.exceptions.BatchException;
import com.mcg.batch.store.ArchivalInstanceDetails;
import com.mcg.batch.store.Archiver;
import com.mcg.batch.store.BatchMetadataStore;
import com.mcg.batch.utils.CollectionUtils;
import com.mcg.batch.utils.StackUtils;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class RedisArchiver implements Archiver {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RedisArchiver.class);

	private SmartBatchRedisRuntimeStore batchStore = null;
	private BatchMetadataStore batchMetaStore = null;
	private ArrayList<String> batchNames;
	private Long startEpoch;
	private Long endEpoch;
	private Long archivalSeq;
	private String dumpFile;
	private int operation = 0;
	private String nameSpace = null;
	private String description;
	private String type;

	/**
	 * 
	 */
	private RedisArchiver() {
		nameSpace = ThreadContextUtils.getNamespace();
	}

	/**
	 * @param batchStore
	 * @param batchMetaStore
	 * @param batchNames
	 * @param startEpoch
	 * @param endEpoch
	 * @param archivalSeq
	 */
	public RedisArchiver(SmartBatchRedisRuntimeStore batchStore,
			BatchMetadataStore batchMetaStore, ArrayList<String> batchNames,
			Long startEpoch, Long endEpoch, Long archivalSeq, int operation,
			String description, String type) {
		this();
		this.batchStore = batchStore;
		this.batchMetaStore = batchMetaStore;
		this.batchNames = batchNames;
		this.startEpoch = startEpoch;
		this.endEpoch = endEpoch;
		this.archivalSeq = archivalSeq;
		this.operation = operation;
		this.description = description;
		this.type = type;

	}

	/**
	 * @param batchStore
	 * @param batchMetaStore
	 * @param archivalSeq
	 * @param dumpFile
	 * @param operation
	 */
	public RedisArchiver(SmartBatchRedisRuntimeStore batchStore,
			BatchMetadataStore batchMetaStore, Long archivalSeq,
			String dumpFile, int operation) {
		this();
		this.batchStore = batchStore;
		this.batchMetaStore = batchMetaStore;
		this.archivalSeq = archivalSeq;
		this.dumpFile = dumpFile;
		this.operation = operation;
		this.description = "Restoring The Archive from dump file.";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.Archiver#loadFromArchive()
	 */
	@Override
	public void doRestore() throws BatchException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchRedisRuntimeStore.loadArchive() started");
		}
		File file = null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		ArchivalRecord archivedRecord = null;

		try {
			file = new File(dumpFile);
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			if (ois.readInt() == 0) {
				while (ois.readInt() > 0) {
					archivedRecord = (ArchivalRecord) ois.readObject();
					batchStore.put(archivedRecord.getJobInstance());
					batchStore.put(archivedRecord.getJobExecution());
					batchStore.saveExecutionContext(archivedRecord
							.getJobExecution());
					batchStore.put(archivedRecord.getJobExecution().getId(),
							archivedRecord.getJobParameters());
					if (CollectionUtils.isNotEmpty(archivedRecord
							.getStepExecutions())) {
						for (StepExecution stepExecution : archivedRecord
								.getStepExecutions()) {
							batchStore.put(stepExecution);
							batchStore.saveExecutionContext(stepExecution);
						}
					}
					archivedRecord = null;

				}
			}

		} catch (EOFException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The EOF for the file has reached. Discontinuing Reading");
			}
		} catch (FileNotFoundException e) {
			throw new BatchException(e);
		} catch (IOException e) {
			throw new BatchException(e);
		} catch (ClassNotFoundException e) {
			throw new BatchException(e);
		} finally {
			archivedRecord = null;
			close(ois, fis);
			ois = null;
			fis = null;
			file = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchRedisRuntimeStore.loadArchive() completed");
			}
		}
	}

	static final long toGMTEpoch(Date date) {

		TimeZone tz = TimeZone.getDefault();
		Date ret = new Date(date.getTime() - tz.getRawOffset());

		if (tz.inDaylightTime(ret)) {
			Date dstDate = new Date(ret.getTime() - tz.getDSTSavings());
			if (tz.inDaylightTime(dstDate)) {
				ret = dstDate;
			}
		}
		return ret.getTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.store.Archiver#doArchive()
	 */
	@Override
	public void doArchive() throws BatchException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisArchiver.doArchive() started");
		}
		Long minRestartable = 30L;
		List<JobInstance> jobInstances = new ArrayList<JobInstance>();
		List<String> batchesToInclude = new ArrayList<String>();
		List<Long> jobExecutionIds = null;
		JobExecution jobExecution = null;
		List<StepExecution> stepExecutions = null;
		ExecutionContext jobExecutionContext = null;
		List<ExecutionContext> stepExecutionContexts = null;
		File file = null;
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		ArchivalRecord record = null;
		JobInstance jobInstancetmp = null;
		boolean found = false;
		int counter = 0;

		try {

			file = new File(new File(BatchConfiguration.ARCHIVE_DIRECTORY),
					concat("archive-", archivalSeq, ".dat"));
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeInt(counter);
			if(LOGGER.isDebugEnabled())
            {
                LOGGER.debug((new StringBuilder("Batch Names list is Empty : ")).append(CollectionUtils.isEmpty(batchNames)).toString());
                LOGGER.debug((new StringBuilder("Batch Names list in Redis Archiver : ")).append(batchNames.toString()).toString());
            }
			if (isEmpty(batchNames)) {
				batchesToInclude.addAll(batchMetaStore
						.getTotalBatchesRegistered());
			} else {
				batchesToInclude.addAll(batchNames);
			}
			for (String batchName : batchesToInclude) {
				jobInstances.addAll(batchStore.getJobInstances(batchName, 0,
						(int) SmartBatchRedisRuntimeStore.getCache()
						.incrementAndGet(BATCH_JOB_SEQ, 0)));


				//Sathish Change To Store Batch Last Execution Date In Definition - Start

				Long instanceIdForLastExec = batchStore.getLastExecutedInstanceId(batchName);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Examining the batch : " + batchName + " With ID : " + instanceIdForLastExec);
				}
				if (null!=instanceIdForLastExec && instanceIdForLastExec > 0) {
					JobInstance jobInstanceForLastExec = batchStore.getJobInstance(instanceIdForLastExec);
					if (jobInstanceForLastExec != null) {
						JobExecution jobExecutionForLastExec = batchStore.getLastJobExecution(jobInstanceForLastExec);
						if (jobExecutionForLastExec != null) {
							if(jobExecutionForLastExec.getStartTime()!=null){
								BatchDefinitionBean betchDefBean=batchMetaStore.getBatchDefniton(batchName);
								betchDefBean.getParams().put("LASTEXECUTIONDATE", jobExecutionForLastExec.getStartTime());
								batchMetaStore.registerBatch(betchDefBean);
							}
						}
					}
				}
				//Sathish Change To Store Batch Last Execution Date In Definition - End

			}
			
			 if(LOGGER.isDebugEnabled())
			 {
	                LOGGER.debug("Job Instances found for selected batches : " + jobInstances.size());
			 }
	           

			for (Iterator<JobInstance> ite = jobInstances.iterator(); ite
					.hasNext();) {
				jobInstancetmp = ite.next();
				jobExecutionIds = new ArrayList<Long>(
						batchStore.getJobExecutionsIds(jobInstancetmp.getId()));
				if(LOGGER.isDebugEnabled())
				{
                    LOGGER.debug("Job Executions found for instance : " + jobExecutionIds.size());
				}
				found = false;
				for (Long jobExecutionId : jobExecutionIds) {
					jobExecution = batchStore.getJobExecution(jobExecutionId);
					minRestartable = getRestartableValue(jobExecution);

					if (jobExecution != null
							&& (jobExecution.getStatus() == BatchStatus.COMPLETED || jobExecution
									.getStatus() == BatchStatus.FAILED
									&& (System.currentTimeMillis()
											- jobExecution.getLastUpdated()
													.getTime() > minRestartable))
							&& jobExecution.getLastUpdated().getTime() > startEpoch
							&& jobExecution.getLastUpdated().getTime() < endEpoch) {

						found = true;
						jobExecution = null;
						break;
					}
					jobExecution = null;
				}

				if (!found) {
					ite.remove();
				}
				jobInstancetmp = null;
			}

			for (JobInstance jobInstance : jobInstances) {
				jobExecutionIds = new ArrayList<Long>(
						batchStore.getJobExecutionsIds(jobInstance.getId()));
				 if(LOGGER.isDebugEnabled())
				 {
	                    LOGGER.debug("Job Executions found for instance : " + jobExecutionIds.size());
				 }
				for (Long jobExecutionId : jobExecutionIds) {
					jobExecution = batchStore.getJobExecution(jobExecutionId);
					stepExecutions = (List<StepExecution>) jobExecution
							.getStepExecutions();
					jobExecutionContext = batchStore
							.getExecutionContext(jobExecution);
					if (stepExecutions != null) {
						stepExecutionContexts = new ArrayList<ExecutionContext>();
						for (StepExecution stepExecution : stepExecutions) {
							stepExecutionContexts.add(batchStore
									.getExecutionContext(stepExecution));
							SmartBatchRedisRuntimeStore.getCache()
									.removeFromMap(BATCH_STEP_EXECUTION,
											stepExecution.getId());
							SmartBatchRedisRuntimeStore.getCache()
									.removeFromMap(
											BATCH_STEP_EXECUTION_CONTEXT,
											stepExecution.getId());

							stepExecution = null;
						}
					}

					record = new ArchivalRecord(jobInstance, jobExecution,
							stepExecutions, jobExecutionContext,
							stepExecutionContexts,
							batchStore.getJobParamters(jobExecution.getId()));
					 if(LOGGER.isDebugEnabled())
					 {
	                        LOGGER.debug("Archiving Execution : " +jobExecution.getJobId());
					 } 
					oos.writeInt(++counter);
					oos.writeObject(record);
					SmartBatchRedisRuntimeStore.getCache().removeFromMap(
							BATCH_JOB_EXECUTION_CONTEXT, jobExecution.getId());

					SmartBatchRedisRuntimeStore.getCache().removeFromMap(
							BATCH_JOB_EXECUTION, jobExecution.getId());

					SmartBatchRedisRuntimeStore.getCache().removeFromMap(
							BATCH_JOB_EXECUTION_PARAMS, jobExecution.getId());

					SmartBatchRedisRuntimeStore.getCache().removeFromMap(
							BATCH_JOB_INSTANCE, jobInstance.getId());

					SmartBatchRedisRuntimeStore.getCache().remove(
							concat(JOB_INSTANCE_EXEC_ID_LIST_PREFIX,
									jobInstance.getInstanceId()));

					SmartBatchRedisRuntimeStore.getCache().remove(
							concat(JOB_EXEC_INSTANCE_ID_PREFIX,
									jobExecution.getId()));

					SmartBatchRedisRuntimeStore.getCache().removeFromList(
							concat(JOB_NAME_ISTANCE_ID_LIST_PREFIX,
									jobInstance.getJobName()), 0,
							jobInstance.getInstanceId());

					SmartBatchRedisRuntimeStore.getCache().remove(
							concat(JOB_ID_STEP_ID_LIST_PREFIX,
									jobExecution.getId()));

					stepExecutions = null;
					jobExecutionId = null;
					jobExecution = null;
				}
				jobExecutionIds = null;
				jobInstance = null;
			}
		} catch (FileNotFoundException e) {
			throw new BatchException(e);
		} catch (IOException e) {
			throw new BatchException(e);
		} catch (Exception e) {
			e.printStackTrace();
			;
		} finally {
			batchesToInclude = null;
			jobInstances = null;
			close(oos, fos);
			fos = null;
			oos = null;
			file = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisArchiver.doArchive() completed");
			}
		}

	}

	public Long getRestartableValue(JobExecution jobExecution) {
		Long minRestartable = 0L;

		BatchDefinitionBean batchDefinitionBean = (BatchDefinitionBean) SmartBatchRedisRuntimeStore
				.getCache().getFromMap("BATCH_JOBS_DEFINITION",
						jobExecution.getJobInstance().getJobName());
		String value = batchDefinitionBean.getParam(BATCH_RESTARTABLE_AGE,
				String.class);
		if (value != null && value != "") {
			minRestartable = Long.parseLong(value);
		}
		minRestartable = minRestartable * 86400000L;
		return minRestartable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("RedisArchiver.run() started");
		}
		ThreadContextUtils.setRunTimeContext(new SmartBatchRuntimeContext(
				this.nameSpace));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("The Name space at archiver is.... "
					+ ThreadContextUtils.getNamespace());
		}
		ArchivalInstanceDetails details = null;
		try {
			details = new ArchivalInstanceDetails(archivalSeq);
			details.setDescription(description);
			details.setType(type);

			if (operation == ARCHIVE) {
				details.setAction(ARCHIVE_ACTION);
				details.setFileName(concat(
						BatchConfiguration.ARCHIVE_DIRECTORY, File.separator,
						"archive-", archivalSeq, ".dat"));
				doArchive();
				SmartBatchRedisRuntimeStore.getCache().putToMap(
						ARCHIVER_DETAILS, archivalSeq, details);
			} else if (operation == RESTORE) {
				details.setAction(RESTORE_ACTION);
				details.setDescription("Restoration of An old Archive...");
				details.setFileName(dumpFile);
				doRestore();
				SmartBatchRedisRuntimeStore.getCache().putToMap(
						ARCHIVER_DETAILS, archivalSeq, details);
			} else {
				throw new BatchException("Invalid Operation specified");
			}
			details = (ArchivalInstanceDetails) SmartBatchRedisRuntimeStore
					.getCache().getFromMap(ARCHIVER_DETAILS, archivalSeq);
			details.setStatus("COMPLETED");
			details.setLastUpdatedTime(System.currentTimeMillis());
			SmartBatchRedisRuntimeStore.getCache().putToMap(ARCHIVER_DETAILS,
					archivalSeq, details);
		} catch (BatchException e) {
			details = (ArchivalInstanceDetails) SmartBatchRedisRuntimeStore
					.getCache().getFromMap(ARCHIVER_DETAILS, archivalSeq);
			details.setStatus("FAILED");
			details.setLastUpdatedTime(System.currentTimeMillis());
			details.setErrorDetails(StackUtils.throwableToString(e));
			SmartBatchRedisRuntimeStore.getCache().putToMap(ARCHIVER_DETAILS,
					archivalSeq, details);
		} finally {
			details = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("RedisArchiver.run() completed");
			}
		}
	}

	public static void main(String[] args) throws ParseException {
		Date date = new Date();
		String dateStr = "2015-03-27 19:20:19+0800";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
		System.out.println(sdf1.format(date));
		System.out.println(sdf.format(date));
		Date parsedDate = sdf.parse(dateStr);
		System.out.println(sdf.format(parsedDate));

	}

}