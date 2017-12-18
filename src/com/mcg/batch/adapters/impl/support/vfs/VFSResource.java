/**
 * 
 */
package com.mcg.batch.adapters.impl.support.vfs;


import static com.mcg.batch.core.BatchConfiguration.HTTP_DEFAULT_PORT;
import static com.mcg.batch.core.BatchConfiguration.HTTP_PROXY_HOST_KEY;
import static com.mcg.batch.core.BatchConfiguration.HTTP_PROXY_PORT_KEY;
import static com.mcg.batch.core.BatchConfiguration.HTTP_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.SSH_KEY_FILE_NAME;
import static org.apache.commons.vfs2.FileName.SEPARATOR_CHAR;

import java.awt.print.Paper;
import java.net.URI;
import java.security.Policy.Parameters;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.provider.GenericFileName;

import com.mcg.batch.core.BatchConfiguration;

/**
 * VFS Resource is the Adapter definition configured for any VFS Operation.<br>
 * This implementation currently supports<br>
 * 1. FTP(S)<br>
 * 2. HTTP(S)<br>
 * 3. SFTP<br>
 * 4. SMB<br>
 * 5. HDFS<br>
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */

public class VFSResource {

    private String resourceAlias;
    private FileName fileName;
    private Map<String, String> parameters = new HashMap<String, String>();

    /**
	 * 
	 */
    public VFSResource() {
    }

    /**
     * Getter for resourceAlias
     *
     * @return resourceAlias String
     */
    public String getResourceAlias() {
	return resourceAlias;
    }

    /**
     * Setter for resourceAlias
     *
     * @param resourceAlias
     *            String
     */
    public void setResourceAlias(String resourceAlias) {
	this.resourceAlias = resourceAlias;
    }

    /**
     * @return the fileDetails FileName
     */
    public FileName getFileName() {
	return fileName;
    }

    /**
     * @param fileName
     *            FileName
     */
    public void setFileName(final FileName fileName) {
	this.fileName = fileName;
    }

    public String getUserName() {

	return (fileName instanceof GenericFileName) ? ((GenericFileName) fileName)
		.getUserName() : null;

    }

    public String getPassword() {
	return (fileName instanceof GenericFileName) ? (((GenericFileName) fileName)
		.getPassword()) : null;
    }

    public URI getURI() {
	return URI.create(fileName.getURI());
    }

    /**
     * @return the parameters Map<String,String>
     */
    public Map<String, String> getParameters() {
	return parameters;
    }

    /**
     * @param parameters
     *            Map<String,String>
     */
    public void setParameters(final Map<String, String> parameters) {
	this.parameters = parameters;
    }

    public String getParameter(final String name) {
	if (parameters != null) {
	    return parameters.get(name);
	} else {
	    return null;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.vfs2.provider.AbstractFileName#getURI()
     */
    public String getURI(final String fileName) {
	StringBuilder uri = new StringBuilder(this.fileName.getURI());
	if (HTTP_SCHEME.equals(this.fileName.getScheme())
		|| BatchConfiguration.HTTPS_SCHEME.equals(this.fileName
			.getScheme())) {
	    if (uri.charAt(uri.length() - 1) != SEPARATOR_CHAR) {
		uri.append(SEPARATOR_CHAR);
	    }
	    uri.append(fileName);
	}

	return uri.toString();
    }

    public String getHostKeyFile() {

	return getParameter(SSH_KEY_FILE_NAME);
    }

    public String getNTDomain() {
	return getParameter(BatchConfiguration.NT_DOMAIN);
    }

    public String getHttpProxyHost() {
	return parameters.get(HTTP_PROXY_HOST_KEY);
    }

	public int getHttpProxyPort() {
		return parameters.containsKey(BatchConfiguration.HTTP_PROXY_PORT_KEY) ? Integer
				.parseInt(BatchConfiguration.HTTP_PROXY_PORT_KEY)
				: BatchConfiguration.HTTP_DEFAULT_PORT;
	}

    public String getProxyUser() {
	return parameters.get(BatchConfiguration.HTTP_PROXY_AUTH_USER_KEY);
    }

    public String getProxyPassword() {
	return parameters.get(BatchConfiguration.HTTP_PROXY_AUTH_PASSWORD_KEY);
    }

  
    public String getProxyDomain() {
	return parameters.get(BatchConfiguration.HTTP_PROXY_AUTH_DOMAIN_KEY);
    }

 
    public int getBatchVFSThreshold() {

	String threshold = getParameter(BatchConfiguration.BATCH_VFS_THRESHOLD);
	if (StringUtils.isEmpty(threshold)) {
	    return 0;
	} else {
	    return Integer.parseInt(threshold);
	}
    }

 
}
