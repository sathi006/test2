/**
 * 
 */
package com.mcg.batch.store;

import com.mcg.batch.exceptions.BatchException;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public interface Archiver extends Runnable {
	public static final int ARCHIVE = 1;
	public static final int RESTORE = 2;

	/**
	 * Perform archival of the completed batches.
	 * 
	 * @throws BatchException
	 */
	public void doArchive() throws BatchException;

	/**
	 * Restore runtime info from archive dump.
	 * 
	 * @throws BatchException
	 */
	public void doRestore() throws BatchException;
	

}