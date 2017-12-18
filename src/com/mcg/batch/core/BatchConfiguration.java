/**
 * 
 */
package com.mcg.batch.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcg.batch.adapters.impl.support.vfs.TempFileCleanup;
import com.mcg.batch.runtime.impl.batch.utils.Encryptor;
import com.mcg.batch.utils.IOHelper;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@SuppressWarnings("unchecked")
public class BatchConfiguration {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchConfiguration.class);

	public static final Properties PROPERTIES = new Properties();
	public static final String BATCH_PROPERTIES_FILE_KEY = "watt.scb.smartbatch.properties.file";

	public static Class<? extends Encryptor<String, String>> TEXT_ENCRYPTOR_CLASS = null;

	/**
	 * prevent external instantiation
	 */
	private BatchConfiguration() {
	}

	/**
	 * Static method to load the properties.
	 */
	static {
		PropertyConfigurator.configureAndWatch(System.getProperty(
				"watt.scb.smartbatch.log4j.config.file",
				"smart-batch-log4j.properties"), 10000);

		File file = null;
		FileInputStream fis = null;
		try {
			file = new File(System.getProperty(BATCH_PROPERTIES_FILE_KEY,
					"scb-smart-batch.properties"));
			fis = new FileInputStream(file);
			PROPERTIES.load(fis);
			TEXT_ENCRYPTOR_CLASS = (Class<? extends Encryptor<String, String>>) Class
					.forName(getProperty("batch.encryptor.class"));
		} catch (IOException e) {
			LOGGER.error("Error while loading properties file", e);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Error while generating the encryptor Class", e);
		} finally {
			IOHelper.close(fis);
			file = null;
			fis = null;
		}
	}

	public static final String ENCRYPTION_KEY = getProperty(
			"batch.encryptor.key", "master-key");
	/**
	 * Prefix for the BATCH APPLICATION FILE
	 */
	public static final String BATCH_APPLICATION_CONTEXT_FILE = getProperty("root.applicaton.context.file.location");
	/**
	 * Node Name constant
	 */
	public static final String NODE_NAME = PROPERTIES
			.getProperty("smartbatch.node.alias");
	/**
	 * Registry Version cache key constant
	 */
	public static final String REGISTRY_VERSION = "registry.version.cachename";

	public static final String BATCH_STATUS_ALL = getProperty(
			"batch.execution.status.all", "ANY");

	public static final String CACHE_MASTER_FILE = PROPERTIES
			.getProperty("cacheMasterPath");
	public static final String BATCH_ENVIRONMENT = PROPERTIES
			.getProperty("environment.name");
	public static final String TIMESTAMP_FORMAT = PROPERTIES.getProperty(
			"timestamp.format", "yyyy-mm-dd HH:MM:SS.SSS");
	public static final String AUDIT_INFO_DESTINATION = PROPERTIES.getProperty(
			"batch.audit.jms.info.destination", "audit-info-destination");
	public static final String NOTIFICATION_DESTINATION = PROPERTIES.getProperty(
			"batch.notification.jms.destination", "notification-destination");
	public static final String AUDIT_ERROR_DESTINATION = PROPERTIES
			.getProperty("batch.audit.jms.error.destination",
					"audit-error-destination");
	public static final String SFTP_SCHEME = "sftp";
	public static final String FTP_SCHEME = "ftp";
	public static final String FTPS_SCHEME = "ftps";
	public static final String HTTP_SCHEME = "http";
	public static final String HTTPS_SCHEME = "https";
	public static final String WEBDAV_SCHEME = "webdav";
	public static final String LOCAL_FS_SCHEME = "file";
	public static final String ZIP_FS_SCHEME = "zip";
	public static final String JAR_FS_SCHEME = "jar";
	public static final String TAR_FS_SCHEME = "tar";
	public static final String TGZ_FS_SCHEME = "tgz";
	public static final String TBZ2_FS_SCHEME = "tbz2";
	public static final String HDFS_SCHEME = "hdfs";
	public static final String CIFS_SCHEME = "smb";
	public static final String TMP_FS_SCHEME = "tmp";
	public static final String RAM_FS_SCHEME = "ram";
	public static final String RES_FS_SCHEME = "res";
	public static final String NO = "no";
	public static final String YES = "yes";

	

	public static final String RSYNC_MOVE_OPTION = getProperty(
			"batch.rsync.move.option", "--remove-source-files");
	public static final String ARCHIVE_DIRECTORY = getProperty(
			"batch.archive.directory", "/tmp");
	public static final String SSH_KEY_FILE_NAME = "ssh.host.key.file";
	public static final String HTTP_PROXY_HOST_KEY = "http.proxy.host";
	public static final String HTTP_PROXY_PORT_KEY = "http.proxy.port";
	public static final String HTTP_PROXY_AUTH_USER_KEY = "http.proxy.auth.user";
	public static final String HTTP_PROXY_AUTH_PASSWORD_KEY = "http.proxy.auth.password";
	public static final String HTTP_PROXY_AUTH_DOMAIN_KEY = "http.proxy.auth.domain";
	public static final String SFTP_COMPRESSION_KEY = "sftp.compression";
	public static final String FTP_TRANSFER_MODE_KEY = "ftp.transfer.mode";
	
	public static final String NT_DOMAIN = "NT_DOMAIN";
	public static final String ADAPTER_SPRING_TRANSFROM_XSL = getProperty("batch.adapter.defn.xsl");

	public static final String BATCH_VFS_PROVIDERS = getProperty(
			"vfs.providers.config.file",
			"classpath:org/apache/commons/vfs2/impl.providers.xml");
	
	public static final String JCIFS_PROPERTIES = "jcifs.properties";
	
	public static final String BATCH_VFS_PARAM_MAX_FILE_SIZE = "vfs.transfer.max.file.size";
	public static final String BATCH_VFS_PARAM_MAX_FILE_COUNT = "vfs.transfer.max.file.count";
	public static final String BATCH_VFS_PARAM_FILTER_SORY_ORDER = "vfs.transfer.filter.sort.order";
	public static final String BATCH_VFS_PARAM_FILTER_SORY_BY = "vfs.transfer.filter.sort.by";
	public static final String BATCH_VFS_PARAM_CHECK_SOURCE = "vfs.transfer.check.source";
	public static final String BATCH_VFS_PARAM_TRANFER_TYPE = "vfs.transfer.type";
	public static final String BATCH_VFS_PARAM_FILE_NAME = "vfs.transfer.file.name";
	public static final String BATCH_VFS_SRC_PRE_SCRIPT = "vfs.source.pre.processing.script";
	public static final String BATCH_VFS_SRC_POST_SCRIPT = "vfs.source.post.processing.script";
	public static final String BATCH_VFS_TGT_PRE_SCRIPT = "vfs.target.pre.processing.script";
	public static final String BATCH_VFS_TGT_POST_SCRIPT = "vfs.target.post.processing.script";
	public static final String BATCH_VFS_MIN_FILE_AGE = "vfs.min.file.age";
	public static final String BATCH_VFS_MIN_AGE_ACTION = "vfs.min.file.age.action";
	public static final String BATCH_VFS_LOCAL_TEMP_LOCATION = getProperty("batch.file.temp.location");
	public static final TempFileCleanup VFS_TEMP_FILE_ACTION = TempFileCleanup
			.valueOf(getProperty("batch.file.temp.cleanup.action", "NONE"));
	
	public static final String BATCH_VFS_LOCAL_TEMP_ARCHIVE_LOCATION=getProperty("batch.file.temp.archive.location","/tmp");
	public static final String BATCH_VFS_IS_SINGLE_FILE="vfs.is.single.file";
	public static final String BATCH_VFS_TARGET_FILE_NAME="vfs.target.file.name";
	public static final String BATCH_VFS_TARGET_RELATIVE_PATH = "vfs.target.relative.folder.path";

	public static final boolean DOMAIN_SPECIFIC_STORE = getBooleanProperty(
			"batch.domain.specific.store", "false");
	public static final boolean USE_DOMAIN_SEQUENCE = getBooleanProperty(
			"batch.domain.sequence", "false");

	public static final String BATCH_RSYNC_PARAM_TRANFER_TYPE = "rsync.transfer.type";
	public static final String BATCH_RSYNC_PARAM_FILE_NAME = "rsync.transfer.file.name";
	public static final String BATCH_RSYNC_PARAM_INCLUDE_FILTER = "rsync.transfer.filter.include";
	public static final String BATCH_RSYNC_PARAM_EXCLUDE_FILTER = "rsync.transfer.filter.exclude";

	public static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
	public static final String BATCH_DEPENDENT_ADAPTERS_LABEL = "dependentAdapters";
	public static final int THREAD_POOL_MAX = getIntProperty("framework.threadpool.max");
	public static final int THREAD_POOL_MIN = getIntProperty("framework.threadpool.min");
	public static final int THREAD_POOL_BACKLOG = getIntProperty("framework.threadpool.backlog");
	public static final long THREAD_POOL_KEEP_ALIVE_MS = getLongProperty("framework.threadpool.keepalive.milliseconds");
	public static final String BATCH_VFS_THRESHOLD = "vfs.target.fs.usage.threshold";
	public static final String BATCH_FAULTY_RESOURCE = "faultyResource";
	public static final String FILE_NAME_FILTER_STATUS = "fileNameFilterStatus";
	public static final String FILE_SIZE_FILTER_STATUS = "fileSizeFilterStatus";
	public static final String FILE_COUNT_FILTER_STATUS = "fileCountFilterStatus";
	

	public static final long BATCH_RESTART_FREQUENCY = getLongProperty("batch.restart.frequency.ms","86400000");

	public static final int DEFAULT_SEARCH_LIMIT = getIntProperty(
			"batch.search.default.limit", "1000");
	

	public static final int FTP_DEFAULT_PORT = getIntProperty(
			"ftp.default.port", "21");
	public static final int FTPS_DEFAULT_PORT = getIntProperty(
			"ftps.default.port", "990");
	public static final int SFTP_DEFAULT_PORT = getIntProperty(
			"sftp.default.port", "22");
	public static final int HTTP_DEFAULT_PORT = getIntProperty(
			"http.default.port", "80");
	public static final int HTTPS_DEFAULT_PORT = getIntProperty(
			"http.default.port", "443");
	public static final String BATCH_EXECUTION_WARNINGS = "BATCH_EXECUTION_WARNINGS";
	public static final String BATCH_RESTARTABLE_AGE = 	"minRestartableAge";

	// public static final List<String> KNOWN_NAME_SPACES = Arrays
	// .asList(getProperty("namespaces.list").split(","));

	public static final String getProperty(final String propertyName) {
		return PROPERTIES.getProperty(propertyName);
	}

	public static final int getIntProperty(final String propertyName) {
		return Integer.parseInt(getProperty(propertyName));
	}

	public static final float getFloatProperty(final String propertyName) {
		return Float.parseFloat(getProperty(propertyName));
	}

	public static final long getLongProperty(final String propertyName) {
		return Long.parseLong(getProperty(propertyName));
	}

	public static final double getDoubleProperty(final String propertyName) {
		return Double.parseDouble(getProperty(propertyName));
	}

	public static final boolean getBooleanProperty(final String propertyName) {
		return Boolean.valueOf(getProperty(propertyName));
	}

	public static final String getProperty(final String propertyName,
			final String defaultVal) {
		return PROPERTIES.getProperty(propertyName, defaultVal);
	}

	public static final int getIntProperty(final String propertyName,
			final String defaultVal) {
		return Integer.parseInt(getProperty(propertyName, defaultVal));
	}

	public static final float getFloatProperty(final String propertyName,
			final String defaultVal) {
		return Float.parseFloat(getProperty(propertyName, defaultVal));
	}

	public static final long getLongProperty(final String propertyName,
			final String defaultVal) {
		return Long.parseLong(getProperty(propertyName, defaultVal));
	}

	public static final double getDoubleProperty(final String propertyName,
			final String defaultVal) {
		return Double.parseDouble(getProperty(propertyName, defaultVal));
	}

	public static final boolean getBooleanProperty(final String propertyName,
			final String defaultVal) {
		return Boolean.valueOf(getProperty(propertyName, defaultVal));
	}

}
