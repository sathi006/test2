/**
 * 
 */
package com.mcg.batch.adapters.impl.support.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class BatchFileSelectInfo implements FileSelectInfo {
	private FileObject baseFolder;
	private FileObject file;
	private int depth;

	public FileObject getBaseFolder() {
		return this.baseFolder;
	}

	public void setBaseFolder(FileObject baseFolder) {
		this.baseFolder = baseFolder;
	}

	public FileObject getFile() {
		return this.file;
	}

	public void setFile(FileObject file) {
		this.file = file;
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
}