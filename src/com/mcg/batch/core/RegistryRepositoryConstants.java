/**
 * 
 */
package com.mcg.batch.core;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class RegistryRepositoryConstants {

	/**
	 * prevent external instantiation
	 */
	private RegistryRepositoryConstants() {
	}

	public static final String BATCH_JOB_INSTANCE_DAO_KEY = "JobInstanceDao";

	public static final String BATCH_EXECUTION_CONTEXT_DAO_KEY = "BatchExecutionContextDao";

	public static final String BATCH_JOB_EXECUTION_DAO_KEY = "BatchExecutionContextDao";

	public static final String BATCH_STEP_EXECUTION_DAO_BY_STEP_KEY = "batchStepExecutionDaoByStep";

	public static final String BATCH_STEP_EXECUTION_DAO_BY_JOB_KEY = "batchStepExecutionDaoByJob";

	public static final String BEAN_DEFINITION_CACHE = "beans.defintion.cache";

	public static final String BATCH_DEFINITION_CACHE = "batch.defn.cache";

}
