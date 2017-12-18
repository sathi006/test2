/**
 * 
 */
package com.mcg.batch.adapters.impl.support.vfs;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class FileCountFilter implements FileFilter {


	private AtomicInteger counter = null;

	/**
	 * 
	 */
	public FileCountFilter(int maxCount) {
		counter = new AtomicInteger(maxCount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.vfs2.FileFilter#accept(org.apache.commons.vfs2.
	 * FileSelectInfo)
	 */
	@Override
	public boolean accept(FileSelectInfo fileSelectInfo) {

		return counter.decrementAndGet() >= 0;
	}
}
