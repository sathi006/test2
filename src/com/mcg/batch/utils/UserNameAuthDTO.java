/**
 * 
 */
package com.mcg.batch.utils;

/**
 * A helper DTO for holding username based authentication info.<br>
 * it is expected to be autowired using the xml configuration.
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */

public class UserNameAuthDTO {

	private String userName;
	private String password;
	private String domain;

	/**
	 * @param userName
	 */
	public UserNameAuthDTO(String userName) {
		super();
		this.userName = userName;
	}

	/**
	 * @param userName
	 * @param password
	 */
	public UserNameAuthDTO(String userName, String password) {
		super();
		this.userName = userName;
		this.password = password;
	}

	/**
	 * @param userName
	 * @param password
	 * @param domain
	 */
	public UserNameAuthDTO(String userName, String password, String domain) {
		super();
		this.userName = userName;
		this.password = password;
		this.domain = domain;
	}

	/**
	 * @return the userName String
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            String
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password String
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            String
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the domain String
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain
	 *            String
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

}
