package com.mcg.batch.utils;

import java.util.UUID;

/**
 *
 * Unique ID Generator for appending to Job Name.
 *
 */
public final class UniqueIdGenerator {

	/**
	 * prevent external instantiation
	 */
	private UniqueIdGenerator() {
	}

	/**
	 *
	 * @return Long
	 */
	public static  Long getUUIDAsLong() {
		Long l = UUID.randomUUID().getMostSignificantBits();
		String id = l.toString().replaceAll("-", "").toUpperCase();
		return Long.valueOf(id);
	}

	public static String getUUIDAsString() {
		return UUID.randomUUID().toString();
	}
}
