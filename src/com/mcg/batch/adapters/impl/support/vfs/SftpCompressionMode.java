/**
 * 
 */
package com.mcg.batch.adapters.impl.support.vfs;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public enum SftpCompressionMode {
	ZLIB(1, "zlib,none"), NONE(2, "none");

	private final int compressionModeType;
	private final String compressionMode;

	/**
	 * @param compressionModeType
	 * @param compressionMode
	 */
	private SftpCompressionMode(int compressionModeType, String compressionMode) {
		this.compressionModeType = compressionModeType;
		this.compressionMode = compressionMode;
	}

	/**
	 * @return the compressionModeType
	 */
	public int getCompressionModeType() {
		return compressionModeType;
	}

	/**
	 * @return the compressionMode
	 */
	public String getCompressionMode() {
		return compressionMode;
	}

	/**
	 * Get the Enum based on the String value
	 * 
	 * @param compressionMode
	 * @return
	 */
	public static final SftpCompressionMode get(final String compressionMode) {

		if (ZLIB.getCompressionMode().equalsIgnoreCase(compressionMode)) {
			return ZLIB;
		} else {
			return NONE;
		}

	}

}
