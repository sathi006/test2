/**
 * 
 */
package com.mcg.batch.core.kernel;


import static com.mcg.batch.core.BatchWiringConstants.BATCH_CONTROLLER_COMPONENT;
import static com.mcg.batch.core.support.threading.SmartBatchExecutor.getExecutor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.mcg.batch.admin.BatchController;
import com.mcg.batch.core.BatchConfiguration;
import com.mcg.batch.core.ContextFactory;
import com.mcg.batch.exceptions.AdapterException;
import com.mcg.batch.exceptions.KernelException;

/**
 * 
 * Entry Point for Batch Framework.
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class BatchKernel {
	/**
	 * Logger to be used by this class
	 */
	private static Logger LOGGER = LoggerFactory
			.getLogger(BatchKernel.class);

	// private static final ConcurrentHashMap<String, BatchController>
	// CONTROLLER_PER_NAMESPACE = new ConcurrentHashMap<String,
	// BatchController>();
	// private static final ConcurrentHashMap<String, ConcurrentHashMap<String,
	// Method>> NAMED_METHOD_LIST_PER_NS = new ConcurrentHashMap<String,
	// ConcurrentHashMap<String, Method>>();
	private static BatchController batchController = null;
	private static final ConcurrentHashMap<String, Method> BATCH_CONTROLLER_METHODS = new ConcurrentHashMap<String, Method>();
	private boolean initComplete = false;

	/**
	 * prevent external instantiation
	 */
	private BatchKernel() {

		init();
	}

	/**
	 * 
	 */
	private void init() {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BatchKernel.init() started ");
		}

		Method[] methods = BatchController.class.getDeclaredMethods();
		StringBuilder builder = new StringBuilder();

		for (Method method : methods) {
			builder.setLength(0);
			if (Modifier.isStatic(method.getModifiers())
					|| Modifier.isPrivate(method.getModifiers())) {
				continue;
			}
			builder.append(method.getName());
			Class<?>[] parameterTypes = method.getParameterTypes();
			builder.append(parameterTypes.length);
			for (Class<?> parameterType : parameterTypes) {
				builder.append(parameterType.getName());
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding the method with key " + builder.toString());
			}
			BATCH_CONTROLLER_METHODS.put(builder.toString(), method);
		}
		try {
			//FastClassPathScanner scanner=new FastClassPathScanner();
			//scanner.parseSystemClasspath();
			//scanner.printClassPathElements();
			batchController=ContextFactory.getInstance().aquireContext().getBean(BATCH_CONTROLLER_COMPONENT,
					BatchController.class);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("the batch controller instance is created "+batchController);
			}

			initComplete = true;

			invoke("init", null, "");
			/*System.setProperty("jcifs.resolveOrder", "DNS");
			System.setProperty("jcifs.smb.client.dfs.disabled", "true");*/

			System.setProperty("jcifs.properties", BatchConfiguration.getProperty("jcifs.properties"));
		} catch (KernelException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("KernelException while performing init for batch controller", e);
			}
			e.printStackTrace();
		}
		catch (Exception e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Generic Exception while performing init for batch controller", e);
			}
			e.printStackTrace();
		}

		LOGGER.info("BatchKernel.init() completed");

	}

	/**
	 * Singleton Holder for lazy init
	 * 
	 * @version 1.0
	 * @since:1.0
	 * @author Nanda Gopalan
	 *
	 */
	private static final class SmartBatchKernelInner {
		public static final SmartBatchKernel INSTANCE = new SmartBatchKernel();
	}

	/**
	 * Access to the singleton object
	 * 
	 * @return
	 */
	public static final SmartBatchKernel getInstance() {
		return SmartBatchKernelInner.INSTANCE;
	}

	public BatchController getController() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchKernel.getController() started");
		}
		try {
			return batchController;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchKernel.getController() completed");
			}
		}
	}

	public static final void startup() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchKernel.startup() started");
		}
		try {
			SmartBatchKernel.getInstance();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchKernel.startup() completed");
			}
		}
	}

	/**
	 * 
	 */
	public void shutdown() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchKernel.shutdown() started");
		}
		try {
			getExecutor().shutdownNow();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchKernel.shutdown() completed");
			}
		}

	}

	/**
	 * 
	 * 
	 * @param operationName
	 * @param namespace
	 * @param returnType
	 * @param parameters
	 * @return
	 * @throws AdapterException
	 */

	public <T> T invoke(String operationName, Class<T> returnType,
			String namespace, Object... parameters) throws KernelException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("SmartBatchKernel.invoke() started ");
		}

		Assert.isTrue(initComplete,
				"The init should be complete before an operation can be invoked");
		Assert.notNull(operationName, "Operation Name cannot be null");

		Method method = null;
		StringBuilder builder = null;
		try {
			builder = new StringBuilder(operationName);
			if (parameters != null) {
				builder.append(parameters.length);
				for (Object parameter : parameters) {
					if (parameter instanceof Object[]) {
						builder.append("[L");
					}
					builder.append(parameter.getClass().getName());
				}
			} else {
				builder.append(0);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Retreving method " + builder.toString()
						+ " from cache");
			}
			method = BATCH_CONTROLLER_METHODS.get(builder.toString());
			Assert.notNull(method, "The method by name " + operationName
					+ " cannot be found with the the parameters specified");
			Future<T> future = getExecutor().submit(
					new KernelTask<T>(method, namespace, batchController,
							returnType, parameters));
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The Task is submitted to the pool");
			}
			return future.get();
		} catch (Exception e) {
			throw new KernelException("Unable to invoke the operation "
					+ operationName + " with the parameters provided", e);
		} finally {
			builder = null;

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("SmartBatchKernel.invoke() completed");
			}
		}

	}

	
}
