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
public enum FtpTransferMode {
	ASCII(1, "ASCII"), UTF_8(2, "UTF-8"), UTF_16(3, "UTF-16"), BINARY(4,
			"binary");

	private final int transferModeType;
	private final String transferMode;

	/**
	 * @param transferModeType
	 * @param transferMode
	 */
	private FtpTransferMode(int transferModeType, String transferMode) {
		this.transferModeType = transferModeType;
		this.transferMode = transferMode;
	}

	/**
	 * @return the transferModeType
	 */
	public int getTransferModeType() {
		return transferModeType;
	}

	/**
	 * @return the transferMode
	 */
	public String getTransferMode() {
		return transferMode;
	}

}
