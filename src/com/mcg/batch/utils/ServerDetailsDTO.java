package com.mcg.batch.utils;

public class ServerDetailsDTO {
	private String host;
	private String port;
	private String username;
	private String password;
	/**
	 * @return the host
	 */
	public final String getHost() {
		return host;
	}
	/**
	 * @param host the host to set
	 */
	public final void setHost(String host) {
		this.host = host;
	}
	/**
	 * @return the port
	 */
	public final String getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public final void setPort(String port) {
		this.port = port;
	}
	/**
	 * @return the username
	 */
	public final String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public final void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the password
	 */
	public final String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public final void setPassword(String password) {
		this.password = password;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServerdetailsDTO [host=" + host + ", port=" + port
				+ ", username=" + username + ", password=" + password + "]";
	}
	
	
	
	

}
