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
public class LocalFileName extends
		org.apache.commons.vfs2.provider.local.LocalFileName {
	private static final HashMap<String, Integer> KNOWN_SCHEME_DEFAULT_PORT_NAMES = new HashMap<String, Integer>();

	static {

		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(FTP_SCHEME, FTP_DEFAULT_PORT);
		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(FTPS_SCHEME, FTPS_DEFAULT_PORT);
		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(SFTP_SCHEME, SFTP_DEFAULT_PORT);
		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(HTTP_SCHEME, HTTP_DEFAULT_PORT);
		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(HTTPS_SCHEME, HTTPS_DEFAULT_PORT);
		KNOWN_SCHEME_DEFAULT_PORT_NAMES.put(LOCAL_FS_SCHEME, -1);

	}

	public LocalFileName(final String scheme, final String rootFile,
			final String path) {
		super(scheme, rootFile, path, FileType.FILE_OR_FOLDER);
	}
}
