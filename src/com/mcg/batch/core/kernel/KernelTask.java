/**
 * 
 */
package com.mcg.batch.core.kernel;

import static com.mcg.batch.utils.ReflectionUtils.doInvoke;
import static com.mcg.batch.utils.ThreadContextUtils.clear;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcg.batch.core.support.threading.SmartBatchRuntimeContext;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class KernelTask<T> implements Callable<T> {
	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(KernelTask.class);

	Method operation;
	String namepace;
	Class<T> returnType;
	Object[] parameters;
	Object instance;

	/**
	 * @param operationName
	 * @param nameSpace
	 * @param returnType
	 * @param parameters
	 */
	public KernelTask(Method operation, String nameSpace, Object instance,
			Class<T> returnType, Object... parameters) {
		super();
		this.operation = operation;
		this.namepace = nameSpace;
		this.returnType = returnType;
		this.parameters = parameters;
		this.instance = instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public T call() throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("KernelTask.call() started");
		}
		try {
			ThreadContextUtils.setRunTimeContext(new SmartBatchRuntimeContext(
					namepace));
			return doInvoke(operation, returnType, instance, parameters);

		} finally {
			clear();
			this.operation = null;
			this.namepace = null;
			this.returnType = null;
			this.parameters = null;
			this.instance = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("KernelTask.call() completed");
			}
		}

	}

}
