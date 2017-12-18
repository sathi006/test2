/**
 * 
 */
package com.mcg.batch.adapters.impl.support.vfs;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class FileSizeFilter implements FileFilter {

	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileSizeFilter.class);

	private long maxFileSze;

	/**
	 * @param maxFileSze
	 */
	public FileSizeFilter(long maxFileSze) {
		super();
		this.maxFileSze = maxFileSze;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.vfs2.FileFilter#accept(org.apache.commons.vfs2.
	 * FileSelectInfo)
	 */
	@Override
	public boolean accept(FileSelectInfo fileSelectInfo) {

		try {
			return fileSelectInfo.getFile().getContent().getSize() <= maxFileSze;
		} catch (FileSystemException e) {
			LOGGER.error("Unable to get the file size info", e);
			e.printStackTrace();
		}
		return false;
	}
}
