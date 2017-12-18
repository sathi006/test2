/**
 * 
 */
package com.mcg.batch.core.support.threading;

import org.springframework.batch.item.ExecutionContext;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class SmartBatchRuntimeContext {

	private String namespace = null;
	private Long jobExecutionId = null;
	private Long jobInstanceId = null;
	private ExecutionContext executionContext = null;
	private boolean useNameSpace = true;

	/**
	 * @param namespace
	 */
	public SmartBatchRuntimeContext(String namespace) {
		super();
		this.namespace = namespace;
	}

	/**
	 * @param namespace
	 * @param jobExecutionId
	 * @param jobInstanceId
	 */
	public SmartBatchRuntimeContext(String namespace, Long jobExecutionId,
			Long jobInstanceId) {
		super();
		this.namespace = namespace;
		this.jobExecutionId = jobExecutionId;
		this.jobInstanceId = jobInstanceId;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace
	 *            the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the jobExecutionId
	 */
	public Long getJobExecutionId() {
		return jobExecutionId;
	}

	/**
	 * @param jobExecutionId
	 *            the jobExecutionId to set
	 */
	public void setJobExecutionId(Long jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
	}

	/**
	 * @return the jobInstanceId
	 */
	public Long getJobInstanceId() {
		return jobInstanceId;
	}

	/**
	 * @param jobInstanceId
	 *            the jobInstanceId to set
	 */
	public void setJobInstanceId(Long jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}

	/**
	 * @return the executionContext ExecutionContext
	 */
	public ExecutionContext getExecutionContext() {
		if (this.executionContext == null) {
			setExecutionContext(new ExecutionContext());
		}
		return executionContext;
	}

	/**
	 * @return the useNameSpace boolean
	 */
	public boolean isUseNameSpace() {
		return useNameSpace;
	}

	/**
	 * @param useNameSpace
	 *            boolean
	 */
	public void setUseNameSpace(boolean useNameSpace) {
		this.useNameSpace = useNameSpace;
	}

	/**
	 * @param executionContext
	 *            ExecutionContext
	 */
	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	public void clear() {
		this.namespace = null;
		this.jobExecutionId = null;
		this.jobInstanceId = null;
		this.executionContext = null;
		this.useNameSpace = false;
	}
}
