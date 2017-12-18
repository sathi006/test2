/**
 * 
 */
package com.mcg.batch.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;

import com.mcg.batch.exceptions.BatchException;
import com.mcg.batch.store.impl.redis.SmartBatchRedisRuntimeStore;

/**
 * This interface provides methods for accessing the data from back-end store.
 * All CRUD operations to back end store(Temporary/permanent) is expected to be
 * going through this interface. This interface extends the Data access objects
 * provided by Spring to access the back-end store.
 * 
 * 
 * 
 * Known Implementations : <br>
 * {@link SmartBatchRedisRuntimeStore}
 *
 * @version 1.0
 * @see {@link ExecutionContextDao}, {@link StepExecutionDao},
 *      {@link JobExecutionDao}, {@link JobInstance}
 * @since:1.0 
 * @author: Nanda Gopalan
 */
public interface BatchRuntimeStore extends ExecutionContextDao,
		StepExecutionDao, JobExecutionDao, JobInstanceDao {

	/**
	 * Link the started executionid with the current node.
	 *
	 * @param jobExecutionId the job execution id
	 */
	public void linkNodeStartedJobs(Long jobExecutionId);

	/**
	 * Provides all the started jobs by this node.
	 *
	 * @return the node started job ids
	 */
	public List<String> getNodeStartedJobIds();
	
	/**
	 * Provides all the started jobs by given node name.
	 * @param nodeName
	 * @return
	 */
	public List<String> getNodeStartedJobIds(String nodeName);

	/**
	 * De-Link the started executionid with the current node It may be used if
	 * it is defunct.
	 *
	 * @param jobExecutionId the job execution id
	 */
	public void deLinkNodeStartedJobs(Long jobExecutionId);

	/**
	 * To Link a Job Execution ID already started in other node to another node identified
	 * by Node Name.
	 * @param StartedJobId
	 * @param nodeName
	 */
	public void linkStartedJobIdtoNode(Long StartedJobId, String nodeName);
	
	/**
	 * To delink a Job Execution ID already started in a node so that it could be linked
	 *  to other node.
	 * @param startedJobId
	 * @param nodeName
	 */
	public void delinkStartedJobIdfromNode(Long startedJobId, String nodeName);

	/**
	 * Archive the instances that are completed using the provided filters.
	 *
	 * @param batchNames the batch names
	 * @param startEpoch the start epoch
	 * @param endEpoch the end epoch
	 * @param descripton the descripton
	 * @param type the type
	 * @return the long
	 * @throws BatchException the batch exception
	 */
	public Long archive(final ArrayList<String> batchNames,
			final Long startEpoch, final Long endEpoch, String descripton,
			String type) throws BatchException;

	/**
	 * Restores the archived information about batch instances from the file.
	 *
	 * @param dumpFile the dump file
	 * @return the long
	 * @throws BatchException the batch exception
	 */
	public Long restoreFromArchive(final String dumpFile) throws BatchException;

	/**
	 * Get the Details of an Archival Execution.
	 *
	 * @param id the id
	 * @return the archival details
	 */
	public ArchivalInstanceDetails getArchivalDetails(Long id);

	/**
	 * Get the list of Archival instances.
	 *
	 * @return the archival instances
	 */
	public Set<Long> getArchivalInstances();

	/**
	 * Get the job Execution ids per instance ids.
	 *
	 * @param jobInstanceId the job instance id
	 * @return the job executions ids
	 */
	public List<Long> getJobExecutionsIds(Long jobInstanceId);
	
	/**
	 * Get all batch names along with their last executed time.
	 *
	 * @return the all batch names last execution
	 */
	public List<String> getAllBatchNamesLastExecution();
	
	/**
	 * Get last instanceId of a batch .
	 *
	 * @param batchName the batch name
	 * @return the last executed instance id
	 */
	public Long getLastExecutedInstanceId(String batchName);
		
	/**
	 * Gets the warning for execution id.
	 *
	 * @param jobExecutionId the job execution id
	 * @return the warning for execution id
	 */
	public String getWarningForExecutionId(final Long jobExecutionId);
	
	/**
	 * Removes the warning for execution id.
	 *
	 * @param jobExecutionId the job execution id
	 */
	public void removeWarningForExecutionId(final Long jobExecutionId);

	
}
