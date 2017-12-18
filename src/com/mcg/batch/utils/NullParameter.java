/**
 * 
 */
package com.mcg.batch.utils;

import com.mcg.batch.adapter.SmartBatchAdapter;

/**
 * 
 * A utility class for null Parameter invoke Methods dynamically.<br>
 * This is for invoking adapter methods dynamically with re-trys.
 * 
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 * @see {@link SmartBatchAdapter}
 */
public final class NullParameter {

	/**
	 * NULL Paramater
	 */
	public static final NullParameter NULL = new NullParameter();

	/**
	 * prevent external instantiation
	 */
	private NullParameter() {
	}
}
