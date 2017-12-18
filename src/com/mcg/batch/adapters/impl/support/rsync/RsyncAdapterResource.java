/**
 * 
 */
package com.mcg.batch.adapters.impl.support.rsync;



/**
 * 
 * A Resouce bean for rsync adapter. This bean contains all the information
 * required for the rsync adapter bean defined at UI.
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class RsyncAdapterResource {

	private String adapterName;
	private String path;
	private String host;
	private String userName;
	private String sshKeyFilePath;

	/**
	 * @return the adapterName String
	 */
	public String getAdapterName() {
		return adapterName;
	}

	/**
	 * @param adapterName
	 *            String
	 */
	public void setAdapterName(String adapterName) {
		this.adapterName = adapterName;
	}

	/**
	 * @return the path String
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            String
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the host String
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            String
	 */
	public void setHost(String host) {
		this.host = host;
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
	 * @return the sshKeyFilePath String
	 */
	public String getSshKeyFilePath() {
		return sshKeyFilePath;
	}

	/**
	 * @param sshKeyFilePath
	 *            String
	 */
	public void setSshKeyFilePath(String sshKeyFilePath) {
		this.sshKeyFilePath = sshKeyFilePath;
	}



}
