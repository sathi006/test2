/**
 * 
 */
package com.mcg.batch.adapters.impl.support.vfs;

import static com.mcg.batch.core.BatchConfiguration.FTPS_DEFAULT_PORT;
import static com.mcg.batch.core.BatchConfiguration.FTPS_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.FTP_DEFAULT_PORT;
import static com.mcg.batch.core.BatchConfiguration.FTP_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.HTTPS_DEFAULT_PORT;
import static com.mcg.batch.core.BatchConfiguration.HTTPS_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.HTTP_DEFAULT_PORT;
import static com.mcg.batch.core.BatchConfiguration.HTTP_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.LOCAL_FS_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.SFTP_DEFAULT_PORT;
import static com.mcg.batch.core.BatchConfiguration.SFTP_SCHEME;

import java.util.HashMap;

import org.apache.commons.vfs2.FileType;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class GenericFileName extends
		org.apache.commons.vfs2.provider.GenericFileName {

	private static final HashMap<String, Integer> KNOWN_SCHEME_DEFAULT_PORT_NAMES = new HashMap<String, Integer>();

	static {

		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(FTP_SCHEME, FTP_DEFAULT_PORT);
		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(FTPS_SCHEME, FTPS_DEFAULT_PORT);
		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(SFTP_SCHEME, SFTP_DEFAULT_PORT);
		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(HTTP_SCHEME, HTTP_DEFAULT_PORT);
		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(HTTPS_SCHEME, HTTPS_DEFAULT_PORT);
		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(LOCAL_FS_SCHEME, -1);

	}

	/**
	 * 
	 * @param scheme
	 * @param hostName
	 * @param port
	 * @param path
	 */
	public GenericFileName(final String scheme, final String hostName,
			final int port, final String path) {
		super(scheme, hostName, port, defaultPort(scheme), null, null, path,
				FileType.FILE_OR_FOLDER);
	}

	/**
	 * 
	 * @param scheme
	 * @param hostName
	 * @param port
	 * @param userName
	 * @param path
	 */
	public GenericFileName(final String scheme, final String hostName,
			final int port, final String userName, final String path) {
		super(scheme, hostName, port, defaultPort(scheme), userName, null,
				path, FileType.FILE_OR_FOLDER);
	}

	/**
	 * 
	 * @param scheme
	 * @param hostName
	 * @param port
	 * @param userName
	 * @param password
	 * @param path
	 */
	public GenericFileName(final String scheme, final String hostName,
			final int port, final String userName, final String password,
			final String path) {
		super(scheme, hostName, port, defaultPort(scheme), userName, password,
				path, FileType.FILE_OR_FOLDER);
	}

	private static final int defaultPort(String scheme) {
		return KNOWN_SCHEME_DEFAULT_PORT_NAMES.containsKey(scheme) ? KNOWN_SCHEME_DEFAULT_PORT_NAMES
				.get(scheme) : -1;

	}
}
