/**
 * 
 */
package com.mcg.batch.adapter;

import java.io.Closeable;

import com.mcg.batch.adapter.impl.JMSAdapter;
import com.mcg.batch.adapter.impl.VFSAdapter;
import com.mcg.batch.exceptions.AdapterException;

/**
 * Adapter class that can have method dynamic-invoke with a resource that can be
 * auto-wired at runtime<br>
 * 
 * Known Implementations: {@link JMSAdapter} , {@link VFSAdapter}
 * 
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public interface SmartBatchAdapter<R> extends Closeable {

	/**
	 * Invokes the method with the name implemented in the class.
	 * 
	 * @param operationName
	 * @param returnType
	 *            specify the class name or null for void type.
	 * @param parameters
	 * @return
	 * @throws AdapterException
	 */

	public <T> T invoke(String operationName, Class<T> returnType,
			Object... parameters) throws AdapterException;

	/**
	 * Invokes a method indexed instead of invoke by name. This implementation
	 * is expected to be faster than others.
	 * 
	 * @param operationid
	 * @param clazz
	 * @param parameters
	 * @return
	 * @throws AdapterException
	 */
	public <T> T invoke(int operationid, Class<T> clazz, Object... parameters)
			throws AdapterException;

	/**
	 * A method to init the smartbatch
	 * 
	 */
	public void init();

	/**
	 * A method to get the resource
	 * 
	 * @return
	 */
	public R getResource();

	/**
	 * A method to set the resource. It is expected that the resource would be
	 * autowired
	 * 
	 * @param resource
	 */
	public void setResource(R resource);

	/**
	 * This method returns the name of the retryer Bean.
	 * 
	 * @return
	 */
	public String getRetryerId();

	/**
	 * This method sets the retryer bean id.
	 * 
	 * @param retryerId
	 */
	public void setRetryerId(String retryerId);

}
