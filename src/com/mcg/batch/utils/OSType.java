/**
 * 
 */
package com.mcg.batch.utils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public enum OSType {

	UNKNOWN("unknown", 0), UX("ux", 1), WIN_NT("windows", 2);

	private final String osName;
	private final int osType;

	/**
	 * @param osName
	 * @param osType
	 */
	private OSType(String osName, int osType) {
		this.osName = osName;
		this.osType = osType;
	}

	/**
	 * @return the osName
	 */
	public String getOsName() {
		return osName;
	}

	/**
	 * @return the osType
	 */
	public int getOsType() {
		return osType;
	}

	/**
	 * Get the Enum based on the String value
	 * 
	 * @param osType
	 * @return
	 */
	public static final OSType getOsType(String osType) {
		if (UX.getOsName().equalsIgnoreCase(osType)) {
			return UX;
		} else if (WIN_NT.getOsName().equalsIgnoreCase(osType)) {
			return WIN_NT;
		} else {
			return UNKNOWN;
		}
	}

}
