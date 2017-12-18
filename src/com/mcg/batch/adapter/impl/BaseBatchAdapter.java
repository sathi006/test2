/**
 * 
 */
package com.mcg.batch.adapter.impl;

import static com.mcg.batch.core.BatchConfiguration.BATCH_FAULTY_RESOURCE;
import static com.mcg.batch.core.BatchWiringConstants.FRAME_WORK_DEFAULT_RETRYER;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.mcg.batch.adapter.Retryer;
import com.mcg.batch.adapter.SmartBatchAdapter;
import com.mcg.batch.core.ContextFactory;
import com.mcg.batch.exceptions.AdapterException;
import com.mcg.batch.exceptions.NonRetryableExecption;
import com.mcg.batch.utils.StackUtils;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public abstract class BaseBatchAdapter<R> implements SmartBatchAdapter<R> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BaseBatchAdapter.class);

	private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Method>> NAMED_METHOD_LIST = new ConcurrentHashMap<String, ConcurrentHashMap<String, Method>>();

	private static final ConcurrentHashMap<String, ConcurrentHashMap<Integer, Method>> INDEXED_METHOD_LIST = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Method>>();

	protected R resource;

	private String retryerId;

	protected BaseBatchAdapter() {
		init();
	}

	/**
	 * 
	 */
	public void init() {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("init() start for the class "
					+ this.getClass().getName());
		}

		if (NAMED_METHOD_LIST.containsKey(this.getClass().getName())) {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("The methods of the class "
						+ this.getClass().getName() + "is already cached...");
			}
		} else {

			ConcurrentHashMap<String, Method> methodList = new ConcurrentHashMap<String, Method>();
			Method[] methods = this.getClass().getDeclaredMethods();
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

				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Adding the method with key "
							+ builder.toString());
				}
				methodList.put(builder.toString(), method);
			}

			NAMED_METHOD_LIST
					.putIfAbsent(this.getClass().getName(), methodList);

		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("init() completed for the class "
					+ this.getClass().getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.adapter.SmartBatchAdapter#invoke(java.lang.String,
	 * java.lang.Class, java.lang.Object[])
	 */
	@Override
	public <T> T invoke(String operationName, final Class<T> returnType,
			Object... parameters) throws AdapterException {
		Assert.notNull(operationName, "Operation Name cannot be null");

		Method method = null;
		StringBuilder builder = new StringBuilder(operationName);
		if (parameters != null) {
			builder.append(parameters.length);
			for (Object parameter : parameters) {
				builder.append(parameter.getClass().getName());
			}
		} else {
			builder.append(0);
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Retreving method " + builder.toString()
					+ " from cache");
		}
		method = NAMED_METHOD_LIST.get(this.getClass().getName()).get(
				builder.toString());
		Assert.notNull(method, "The method by name " + operationName
				+ "cannot be found with the the parameters specified");

		return doInvoke(method, returnType, parameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.adapter.SmartBatchAdapter#invoke(int,
	 * java.lang.Class, java.lang.Object[])
	 */
	@Override
	public <T> T invoke(final int operationid, final Class<T> returnType,
			Object... parameters) throws AdapterException {
		Method method = INDEXED_METHOD_LIST.get(this.getClass().getName()).get(
				operationid);
		Assert.notNull(method, "The method at index" + operationid
				+ "cannot be found ");

		return doInvoke(method, returnType, parameters);
	}

	/**
	 * Utility Method used by both invoke by name and invoke by id
	 * 
	 * @param method
	 * @param returnType
	 * @param parameters
	 * @return
	 * @throws AdapterException
	 */
	@SuppressWarnings("unchecked")
	private <T> T doInvoke(final Method method, final Class<T> returnType,
			final Object... parameters) throws AdapterException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("BaseBatchAdapter.doInvoke() started");
		}
		Retryer retryer = null;
		try {
			if (retryerId == null
					|| (!ContextFactory.getInstance().aquireContext()
							.containsBean(retryerId))) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("The retryer supplied is not available in adapter context. proceeding with framework retryer...");
				}
				retryerId = FRAME_WORK_DEFAULT_RETRYER;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The retryer id used is " + retryerId);
			}
			retryer = ContextFactory.getInstance().aquireContext()
					.getBean(retryerId, Retryer.class);

			do {
				try {
					return (T) method.invoke(this, parameters);
				} catch (InvocationTargetException e) {
					if (e.getTargetException() instanceof NonRetryableExecption) {
					    if (LOGGER.isErrorEnabled()) {
						LOGGER.error(StackUtils.formatException(ThreadContextUtils.
							getJobExecution().getJobInstance().getJobName(),
							ThreadContextUtils.getExecutionContext().getString(BATCH_FAULTY_RESOURCE, ""),
							"Exception at base batch adapter", e));
					}
					    throw new AdapterException(e);
					} else {
					    if (LOGGER.isErrorEnabled()) {
						LOGGER.error(
							"Retryable Exception at base batch adapter", e);
					}
						retryer.errorOccured(e);

					}

				} catch (IllegalAccessException e) {
				    if (LOGGER.isErrorEnabled()) {
					LOGGER.error(StackUtils.formatException(ThreadContextUtils.
						getJobExecution().getJobInstance().getJobName(),
						ThreadContextUtils.getExecutionContext().getString(BATCH_FAULTY_RESOURCE, ""),
						"Exception at base batch adapter", e));
				        }
					throw new AdapterException(e);
				}
			} while (retryer.shouldRetry());
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(StackUtils.formatException(ThreadContextUtils.
					getJobExecution().getJobInstance().getJobName(),
					ThreadContextUtils.getExecutionContext().getString(BATCH_FAULTY_RESOURCE, ""),
					"Max Number of retries reached and failed to complete the method invoke", null));
			}
			throw new AdapterException(
					"Max Number of retries reached and failed to complete the method invoke");
		} finally {
			retryer = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("BaseBatchAdapter.doInvoke() completed");
			}
		}

	}

	/**
	 * This method registers the method for the adapter class with the index
	 * specified by the implementation
	 * 
	 * @param clazz
	 * @param index
	 * @param method
	 */
	protected static final void addMethod(final Class<?> clazz,
			final int index, final Method method) {

		ConcurrentHashMap<Integer, Method> methodList = null;
		synchronized (INDEXED_METHOD_LIST) {
			if (INDEXED_METHOD_LIST.containsKey(clazz.getName())) {
				methodList = INDEXED_METHOD_LIST.get(clazz.getName());
			} else {
				methodList = new ConcurrentHashMap<Integer, Method>();
			}
			methodList.put(index, method);
			INDEXED_METHOD_LIST.put(clazz.getName(), methodList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.adapter.SmartBatchAdapter#getResource()
	 */
	@Override
	public R getResource() {
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.adapter.SmartBatchAdapter#setResource(java.lang.Object
	 * )
	 */
	@Override
	public void setResource(R resource) {
		this.resource = resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mcg.batch.adapter.SmartBatchAdapter#getRetryerId()
	 */
	@Override
	public String getRetryerId() {
		return this.retryerId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mcg.batch.adapter.SmartBatchAdapter#setRetryerId(java.lang.String
	 * )
	 */
	@Override
	public void setRetryerId(String retryerId) {
		this.retryerId = retryerId;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		// Do Nothing.. Override in child classes if required..
	}

}
