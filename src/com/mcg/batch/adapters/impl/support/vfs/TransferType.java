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
public enum TransferType {

	UNKNOWN(0, "unknown"), CREATE(1, "create"), APPEND(2, "append"), NO_REPLACE(
			3, "noReplace"),RECREATE(4,"recreate"),COPY_OF(5,"copy-of"),OVERWRITE(5,"overwrite");

	private int transferTypeId;

	private String transferType;

	/**
	 * @param transferTypeId
	 * @param transferType
	 */
	private TransferType(int transferTypeId, String transferType) {
		this.transferTypeId = transferTypeId;
		this.transferType = transferType;
	}

	/**
	 * @return the transferTypeId
	 */
	public int getTransferTypeId() {
		return transferTypeId;
	}

	/**
	 * @return the transferType
	 */
	public String getTransferType() {
		return transferType;
	}

}
