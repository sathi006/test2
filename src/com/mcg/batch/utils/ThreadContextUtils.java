package com.mcg.batch.utils;

import static com.mcg.batch.utils.StringHelper.EMPTY_STRING;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;

import com.mcg.batch.core.kernel.SmartBatchKernel;
import com.mcg.batch.core.support.threading.SmartBatchRuntimeContext;

public class ThreadContextUtils {

	private static ThreadLocal<SmartBatchRuntimeContext> runtimecontext = new ThreadLocal<SmartBatchRuntimeContext>();

	
	private ThreadContextUtils() {
	}

	public static final Long getJobInstanceID() {
		if (runtimecontext.get() != null) {
			return runtimecontext.get().getJobInstanceId();
		}
		return null;
	}

	public static final JobExecution getJobExecution() {
		return SmartBatchKernel.getInstance().getController().getBatchStore()
				.getJobExecution(getJobExecutionId());
	}

	public static void setRunTimeContext(SmartBatchRuntimeContext context) {
		runtimecontext.set(context);
	}

	public static SmartBatchRuntimeContext getRunTimeContext() {
		return runtimecontext.get();
	}

	public static final Long getJobExecutionId() {
		if (runtimecontext.get() != null) {
			return runtimecontext.get().getJobExecutionId();
		}
		return null;
	}

	public static final String getJobExecutionIdAsString() {
		if (runtimecontext.get() != null) {
			return runtimecontext.get().getJobExecutionId().toString();
		}
		return EMPTY_STRING;
	}

	
	public static final String getJobInstanceIdAsString() {
		if (runtimecontext.get() != null) {
			return runtimecontext.get().getJobInstanceId().toString();
		}
		return EMPTY_STRING;
	}
	public static final String getNamespace() {

		if (runtimecontext.get() != null) {
			return runtimecontext.get().getNamespace();
		}
		return null;

	}

	public static final void clear() {
		if (runtimecontext.get() != null) {
			runtimecontext.get().clear();
		}
		runtimecontext.set(null);
	}

	public static final void addToExecutionContext(String key, Object value) {
		if (runtimecontext.get() != null) {
			runtimecontext.get().getExecutionContext().put(key, value);
		}
	}

	public static final ExecutionContext getExecutionContext() {
		if (runtimecontext.get() != null) {
			return runtimecontext.get().getExecutionContext();
		} else {
			return null;
		}
	}

	public static final void clearExecutionContext() {
		if (runtimecontext.get() != null) {
			runtimecontext.get().setExecutionContext(null);
		}
	}
}
