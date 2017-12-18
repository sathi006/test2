/**
 * 
 */
package com.mcg.batch.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class MapUtils {

	/**
	 * prevent external instantiation
	 */
	private MapUtils() {
	}

	public static final boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	public static final boolean isNotEmpty(Map<?, ?> map) {
		return map != null && map.size() > 0;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final <T> T putIfAbsent(final Map<Object, T> map,
			final Object key, final T value) {
		if (map instanceof ConcurrentMap) {

			return (T) ((ConcurrentMap) map).putIfAbsent(key, value);
		} else {
			synchronized (map) {
				if (!map.containsKey(key)) {
					return map.put(key, value);
				} else {
					return null;
				}
			}
		}
	}

}
