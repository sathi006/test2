/**
 * 
 */
package com.mcg.batch.utils;

import java.util.Collection;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class CollectionUtils {

	/**
	 * Prevent external instantiations.
	 */
	private CollectionUtils() {

	}

	public static final boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static final boolean isNotEmpty(Collection<?> collection) {
		return collection != null && collection.size() > 0;
	}

	
}
