/**
 * 
 */
package com.mcg.batch.store;

import java.io.Serializable;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class ArchivalInstanceDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6306474504573898871L;

	public static final String ARCHIVER_DETAILS = "ARCHIVER_DETAILS";
	public static final String RESTORE_ACTION = "Restore";
	public static final String ARCHIVE_ACTION = "Archive";

	private Long id;
	private String status;
	private long startedTime;
	private long lastUpdatedTime;
	private String errorDetails;
	private String description;
	private String type;
	private String action;
	private String fileName;

	/**
	 * @param id
	 */
	public ArchivalInstanceDetails(Long id) {
		super();
		this.id = id;
		status = "STARTED";
		startedTime = System.currentTimeMillis();
	}

	/**
	 * @return the errorDetails String
	 */
	public String getErrorDetails() {
		return errorDetails;
	}

	/**
	 * @param errorDetails
	 *            String
	 */
	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}

	/**
	 * @return the id Long
	 */

	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            Long
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the status String
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            String
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the lastUpdatedTime long
	 */
	public long getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	/**
	 * @param lastUpdatedTime
	 *            long
	 */
	public void setLastUpdatedTime(long lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

	/**
	 * @return the startedTime long
	 */
	public long getStartedTime() {
		return startedTime;
	}

	/**
	 * @param startedTime
	 *            long
	 */
	public void setStartedTime(long startedTime) {
		this.startedTime = startedTime;
	}

	/**
	 * @return the description String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            String
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the type String
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            String
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the action String
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action String
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the fileName String
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName String
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	

}
