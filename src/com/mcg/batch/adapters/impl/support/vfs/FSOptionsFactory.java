/**
 * 
 */
package com.mcg.batch.adapters.impl.support.vfs;



import static com.mcg.batch.adapters.impl.support.vfs.FtpTransferMode.BINARY;
import static com.mcg.batch.core.BatchConfiguration.CIFS_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.FTP_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.HDFS_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.HTTPS_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.HTTP_SCHEME;
import static com.mcg.batch.core.BatchConfiguration.NO;
import static com.mcg.batch.core.BatchConfiguration.SFTP_SCHEME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftps.custom.FtpsFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.hdfs.custom.HdfsFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.custom.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcg.batch.core.BatchConfiguration;
import com.mcg.batch.exceptions.BatchException;

import jcifs.Config;

/**
 * The <a href="http://commons.apache.org/proper/commons-vfs/filesystems.html"
 * >Apache VFS</a> library provides a common access to various file system.<br>
 * However few file systems like HTTP(S)/SFTP/FTP(S) requires few additional
 * parameters to use them. This class acts as a factory to these special file
 * systems with specific options that pertains to requested protocol/FileName by
 * the {@link VFSResource}{@link #getFileName()}
 * 
 * @version 3.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class FSOptionsFactory {

    /**
     * Holder for string constant no.
     */

    private static final SftpFileSystemConfigBuilder SFTP_CONFIG_BUILDER = SftpFileSystemConfigBuilder
	    .getInstance();
   private static final FtpFileSystemConfigBuilder FTP_CONFIG_BUILDER = FtpFileSystemConfigBuilder
	    .getInstance();
    private static final HttpFileSystemConfigBuilder HTTP_CONFIG_BUILDER = HttpFileSystemConfigBuilder
	    .getInstance();
    private static final HdfsFileSystemConfigBuilder HDFS_CONFIG_BUIDLER = HdfsFileSystemConfigBuilder
	    .getInstance();


    /**
     * Logger to be used by this class.
     */
    private static final Logger LOGGER = LoggerFactory
	    .getLogger(FSOptionsFactory.class);

    /**
     * prevent external instantiation
     */
    private FSOptionsFactory() {
    }

    public static final FileSystemOptions getFsOptions(
	    final VFSResource vfsResource,
	    final BatchVFSParameters batchFileParameters)
	    throws FileSystemException{
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("FSOptionsFactory.getOptions() started");
	}
	FileSystemOptions fsOptions = new FileSystemOptions();
	try {
	    if (SFTP_SCHEME.equals(vfsResource.getFileName().getScheme())) {
		setSftpOptions(fsOptions, vfsResource, batchFileParameters);
	    } 
	    else if(FTP_SCHEME.equals(vfsResource.getFileName().getScheme()))
            {setFtpOptions(fsOptions, vfsResource, batchFileParameters);
            }
	    else if (HTTP_SCHEME
		    .equals(vfsResource.getFileName().getScheme())
		    || HTTPS_SCHEME.equals(vfsResource.getFileName()
			    .getScheme())|| "webdav".equals(vfsResource.getFileName().getScheme())) {
		setHttpOptions(fsOptions, vfsResource, batchFileParameters);
	    } else if (HDFS_SCHEME
		    .equals(vfsResource.getFileName().getScheme())) {
		setHdfsOptions(fsOptions, vfsResource, batchFileParameters);
	    } else if (CIFS_SCHEME
		    .equals(vfsResource.getFileName().getScheme())) {
		setCifsOptions(fsOptions, vfsResource, batchFileParameters);
	    } 
	} finally {
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("FSOptionsFactory.getOptions() completed");
	    }
	}

	return fsOptions;
    }

    /**
     * @param fsOptions
     * @param vfsResource
     * @param batchFileParameters
     * @throws FileSystemException
     */
    private static void setCifsOptions(FileSystemOptions fsOptions,
	    VFSResource vfsResource, BatchVFSParameters batchFileParameters)
	    throws FileSystemException {
	if (LOGGER.isInfoEnabled()) {
	    LOGGER.info("domain...." + vfsResource.getNTDomain());
	    LOGGER.info("username...." + vfsResource.getUserName());
	    LOGGER.info("CIFS Options : "
		    + Config.getProperty("jcifs.smb.client.connTimeout"));
	    PrintStream stream = new PrintStream(System.out);
	    try {
		Config.list(stream);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    LOGGER.info("CIFS Options : " + stream.toString());
	}
	StaticUserAuthenticator auth = new StaticUserAuthenticator(
		vfsResource.getNTDomain(), vfsResource.getUserName(),
		vfsResource.getPassword());
	DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(
		fsOptions, auth);
    }

    private static void setSftpOptions(FileSystemOptions fsOptions,
	    VFSResource vfsResource,
	    final BatchVFSParameters batchFileParameters)
	    throws FileSystemException {
	SFTP_CONFIG_BUILDER.setStrictHostKeyChecking(fsOptions, NO);
	SFTP_CONFIG_BUILDER.setUserDirIsRoot(fsOptions, false);

	SFTP_CONFIG_BUILDER.setIdentities(fsOptions, new File[] { new File(
		vfsResource.getHostKeyFile())

	});

	SFTP_CONFIG_BUILDER.setCompression(fsOptions, batchFileParameters
		.getSftpCompressionMode().getCompressionMode());
    }

        
    private static void setFtpOptions(FileSystemOptions fsOptions,
    	    VFSResource vfsResource,
    	    final BatchVFSParameters batchFileParameters)
    	    throws FileSystemException {
    	if (batchFileParameters.getFtpTransferMode() != BINARY) {
    	    FTP_CONFIG_BUILDER.setControlEncoding(fsOptions,
    		    batchFileParameters.getFtpTransferMode().getTransferMode());
    	}
        }

    private static void setHdfsOptions(FileSystemOptions fsOptions,
	    VFSResource vfsResource,
	    final BatchVFSParameters batchFileParameters) {
	HDFS_CONFIG_BUIDLER.setUserName(fsOptions, vfsResource.getUserName());
    }

    private static void setHttpOptions(FileSystemOptions fsOptions,
	    VFSResource vfsResource,
	    final BatchVFSParameters batchFileParameters)
	    throws FileSystemException {
	if (vfsResource.getHttpProxyHost() != null)
	    HTTP_CONFIG_BUILDER.setProxyHost(fsOptions,
		    vfsResource.getHttpProxyHost());

	if (vfsResource.getHttpProxyPort() > 0) {
	    HTTP_CONFIG_BUILDER.setProxyPort(fsOptions,
		    vfsResource.getHttpProxyPort());
	}

	if (vfsResource.getHttpProxyHost() != null) {

	    UserAuthenticator authenticator = new StaticUserAuthenticator(
		    vfsResource.getProxyDomain(), vfsResource.getProxyUser(),
		    vfsResource.getProxyPassword());
	    HTTP_CONFIG_BUILDER.setProxyAuthenticator(fsOptions, authenticator);

	}
	
	
    }

 
}
