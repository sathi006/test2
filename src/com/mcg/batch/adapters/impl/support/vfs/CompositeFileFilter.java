/**
 * 
 */
package com.mcg.batch.adapters.impl.support.vfs;

import static com.mcg.batch.core.BatchConfiguration.FILE_COUNT_FILTER_STATUS;
import static com.mcg.batch.core.BatchConfiguration.FILE_NAME_FILTER_STATUS;
import static com.mcg.batch.core.BatchConfiguration.FILE_SIZE_FILTER_STATUS;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class CompositeFileFilter implements FileFilter {
	/**
	 * Logger to be used by this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CompositeFileFilter.class);

	private FileFilter fileNameFilter;
	private FileFilter fileSizeFilter;
	private FileFilter fileCountFilter;

	/**
	 * @param fileNameFilter
	 */
	public CompositeFileFilter(String fileNamePattern) {
		this(-1, -1L, fileNamePattern);
	}

	/**
	 * @param fileNameFilter
	 * @param fileSizeFilter
	 */
	public CompositeFileFilter(final int maxFileCount,
			final String fileNamePattern) {
		this(maxFileCount, -1, fileNamePattern);
	}

	/**
	 * @param fileNameFilter
	 * @param fileCountFilter
	 */
	public CompositeFileFilter(final long maxFileSize,
			final String fileNamePattern) {
		this(-1, maxFileSize, fileNamePattern);

	}

	/**
	 * 
	 * @param maxFileCount
	 * @param maxFileSize
	 * @param fileNamePattern
	 */
	public CompositeFileFilter(final int maxFileCount, final long maxFileSize,
			final String fileNamePattern) {
		// File Name is always expected to be passed hence this would always be
		// there.
		if (fileNamePattern != null && fileNamePattern.length() > 0) {
		    	if (LOGGER.isDebugEnabled()) {
			    LOGGER.debug("File Name Pattern used : " + fileNamePattern);
			}
			this.fileNameFilter = new FileNameFilter(fileNamePattern);
			ThreadContextUtils.addToExecutionContext(FILE_NAME_FILTER_STATUS, false);
		}
		if (maxFileCount > 0) {
			this.fileCountFilter = new FileCountFilter(maxFileCount);
			ThreadContextUtils.addToExecutionContext(FILE_COUNT_FILTER_STATUS, false);
		}
		if (maxFileSize > 0) {
			this.fileSizeFilter = new FileSizeFilter(maxFileSize);
			ThreadContextUtils.addToExecutionContext(FILE_SIZE_FILTER_STATUS, false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.vfs2.FileFilter#accept(org.apache.commons.vfs2.
	 * FileSelectInfo)
	 */
	@Override
	public boolean accept(final FileSelectInfo fileSelectInfo) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("CompositeFileFilter.accept() started");
		}

		boolean accept = true;
		try {
			if (fileNameFilter != null) {
				accept = accept && fileNameFilter.accept(fileSelectInfo);
				if (LOGGER.isDebugEnabled()) {
				    LOGGER.debug("File Name Filter Status ####### " + accept);
				}
				if (ThreadContextUtils.getExecutionContext().containsKey(FILE_NAME_FILTER_STATUS)) {
				    if (!(boolean) ThreadContextUtils.getExecutionContext().get(FILE_NAME_FILTER_STATUS) && accept) {
					ThreadContextUtils.addToExecutionContext(FILE_NAME_FILTER_STATUS, accept);
				    }
				}
			}
			if (accept && fileSizeFilter != null) {
				accept = accept && fileSizeFilter.accept(fileSelectInfo);
				if (LOGGER.isDebugEnabled()) {
				    LOGGER.debug("File Size Filter Status ####### " + accept);
				}
				if (ThreadContextUtils.getExecutionContext().containsKey(FILE_SIZE_FILTER_STATUS)) {
				    if (!(boolean) ThreadContextUtils.getExecutionContext().get(FILE_SIZE_FILTER_STATUS) && accept) {
					ThreadContextUtils.addToExecutionContext(FILE_SIZE_FILTER_STATUS, accept);
				    }
				}
			}
			if (accept && fileCountFilter != null) {
				accept = accept && fileCountFilter.accept(fileSelectInfo);
				if (LOGGER.isDebugEnabled()) {
				    LOGGER.debug("File Count Filter Status ####### " + accept);
				}
				if (ThreadContextUtils.getExecutionContext().containsKey(FILE_COUNT_FILTER_STATUS)) {
				    if (!(boolean) ThreadContextUtils.getExecutionContext().get(FILE_COUNT_FILTER_STATUS) && accept) {
					ThreadContextUtils.addToExecutionContext(FILE_COUNT_FILTER_STATUS, accept);
				    }
				}
			}

			return accept;
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("CompositeFileFilter.accept() completed");
			}
		}

	}
}
