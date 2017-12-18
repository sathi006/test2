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
public class BatchWiringConstants {

	

	/**
	 * prevent external instantiation.
	 */
	private BatchWiringConstants() {

	}

	public static final String BATCH_CACHE_QUALIFIER = "smartBatchCache";
	public static final String REDIS_TEMPLATE_QUALIFIER = "redisTemplate";
	public static final String SINGLETON = "singleton";
	public static final String PROTOTYPE = "prototype";
	public static final String SMART_BATCH_REPOSITORY_COMPONENT = "smartBatchRepository";
	public static final String SMART_BATCH_REGISTRY_COMPONENT = "smartBatchRegistry";
	public static final String BATCH_CONTROLLER_COMPONENT = "smartBatchController";
	public static final String BATCH_LAUNCHER_COMPONENT = "BatchLauncher";
	public static final String SMART_BATCH_TASK_EXECUTOR_COMPONENT = "smartBatchTaskExecutor";
	public static final String SMART_BATCH_EXPLORER_COMPONENT = "batchExplorer";
	public static final String SMART_BATCH_OPERATOR_COMPONENT = "batchOperator";
	public static final String EVENT_BUILDER_COMPONENT = "eventBuilder";
	public static final String BATCH_AUDIT_EVENT_EMITTER_COMPONNET = "batchAuditEventEmitter";
	public static final String BATCH_JMS_LOGGER_COMPONENT = "batchAuditLogger";
	public static final String BATCH_AUDIT_LOGGER_COMPONENT = BATCH_JMS_LOGGER_COMPONENT;
	public static final String BATCH_META_STORE_COMPONENT = "batchMetadataStore";
	public static final String BATCH_RT_STORE_COMPONENT = "batchRuntimeStore";
	public static final String BATCH_JDBC_RT_STORE_DATASOURCE="jdbcStoreRTDataSource";
	public static final String BATCH_JDBC_META_STORE_DATASOURCE="jdbcStoreMetaDataSource";
	public static final String BATCH_JDBC_RT_TRANSACTION_MANAGER = "transactionManager";
	public static final String BATCH_JDBC_META_TRANSACTION_MANAGER="metaTransactionManager";
	public static final String SMART_BATCH_REPOSITORY_FACTORY_COMPONENT = "jobRepository";
	public static final String BATCH_PARAM_INCREMENTOR_COMPONENT = "batchIncrementer";
	public static final String BATCH_LIFECYCLE_LISTENER_COMPONENT = "batchLifeCycleListener";
	public static final String AUDIT_LOGGER_JMS_ADAPTER = "auditLoggerJmsAdapter";
	public static final String FRAME_WORK_DEFAULT_RETRYER = "framework-default-retryer";

}