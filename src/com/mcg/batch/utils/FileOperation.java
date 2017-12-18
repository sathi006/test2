/**
 * 
 */
package com.mcg.batch.utils;

import static com.mcg.batch.utils.StringHelper.concat;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public enum FileOperation {

	UNKNOWN(0, "unknown"), COPY(1, "copy"), MOVE(2, "move");

	private int operationId;
	private String operationName;

	/**
	 * @param operationId
	 * @param operationName
	 */
	private FileOperation(int operationId, String operationName) {
		this.operationId = operationId;
		this.operationName = operationName;
	}

	/**
	 * @return the operationId
	 */
	public int getOperationId() {
		return operationId;
	}

	/**
	 * @return the operationName
	 */
	public String getOperationName() {
		return operationName;
	}

	public static final FileOperation getOperation(final String operationName) {
		if (COPY.getOperationName().equalsIgnoreCase(operationName)) {
			return COPY;
		} else if (MOVE.getOperationName().equalsIgnoreCase(operationName)) {
			return MOVE;
		} else {
			return UNKNOWN;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return concat("The operation Name is ", operationName,
				" and the operation id is ", operationId);
	}
}
