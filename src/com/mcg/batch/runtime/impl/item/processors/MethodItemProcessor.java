/**
 * 
 */
package com.mcg.batch.runtime.impl.item.processors;

import static com.mcg.batch.utils.StringHelper.EQUALS;
import static com.mcg.batch.utils.StringHelper.SEMI_COLON;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.mcg.batch.utils.ReflectionUtils;
import com.mcg.batch.utils.StringHelper;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class MethodItemProcessor<I, O> implements ItemProcessor<I, O> {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MethodItemProcessor.class);

	String transformerClass;
	String methodName;
	String parameters;

	@SuppressWarnings("unchecked")
	public O process(I input) throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("MethodItemProcessor.process() started");
		}
		try {
			List<Object> parameters = new ArrayList<Object>();
			List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
			parameters.add((Object) input);
			parameterTypes.add(Object.class);
			if (StringHelper.isNotEmpty(this.parameters)) {
				String[] params = this.parameters
						.split(SEMI_COLON);
				for (String param : params) {
					parameters.add(param.split(EQUALS)[1]);
					parameterTypes.add(param.getClass());
				}

			}
			return (O) ReflectionUtils.doInvoke(methodName,
					Object.class, transformerClass, parameterTypes.toArray(new Class<?>[parameterTypes.size()]),
					parameters.toArray(new Object[parameters.size()]));
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("MethodItemProcessor.process() completed");
			}
		}
	}

	/**
	 * @return the transformerClass String
	 */
	public String getTransformerClass() {
		return transformerClass;
	}

	/**
	 * @param transformerClass
	 *            String
	 */
	public void setTransformerClass(String transformerClass) {
		this.transformerClass = transformerClass;
	}

	/**
	 * @return the methodName String
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param methodName
	 *            String
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return the parameters String
	 */
	public String getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            String
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

}
