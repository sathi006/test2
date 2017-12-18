/**
 * 
 */
package com.mcg.batch.adapters.impl.support.vfs;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcg.batch.utils.SortBy;
import com.mcg.batch.utils.SortOrder;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class FileObjectSorter {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileObjectSorter.class);

	/**
	 * prevent external instantiation
	 */
	private FileObjectSorter() {
	}

	public static final FileObject[] sortAndGetChildren(
			final FileObject fileObject, final SortOrder sortOrder,
			final SortBy sortBy) throws FileSystemException {
		FileObject[] sortedObjects = null;

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("FileObjectSorter.sortAndGetChildren() started");
		}
		try {
			if (fileObject != null) {
				sortedObjects = fileObject.getChildren();
				if (sortedObjects != null && sortedObjects.length > 0
						&& sortOrder != SortOrder.UNSORTED) {

					switch (sortBy) {
					case NAME: {
						Arrays.sort(sortedObjects,
								new Comparator<FileObject>() {
									/*
									 * (non-Javadoc)
									 * 
									 * @see
									 * java.util.Comparator#compare(java.lang
									 * .Object, java.lang.Object)
									 */
									@Override
									public int compare(FileObject o1,
											FileObject o2) {
										return o1
												.getName()
												.getBaseName()
												.compareTo(
														o2.getName()
																.getBaseName());
									}

								});
						break;
					}
					case SIZE: {
						Arrays.sort(sortedObjects,
								new Comparator<FileObject>() {

									/*
									 * (non-Javadoc)
									 * 
									 * @see
									 * java.util.Comparator#compare(java.lang
									 * .Object, java.lang.Object)
									 */
									@Override
									public int compare(FileObject o1,
											FileObject o2) {
										Long long1 = null;
										Long long2 = null;
										try {
											if (o1.getType() == FileType.FOLDER
													|| o2.getType() == FileType.FOLDER) {
												return o1
														.getName()
														.getBaseName()
														.compareTo(
																o2.getName()
																		.getBaseName());

											} else {
												long1 = o1.getContent()
														.getSize();
												long2 = o2.getContent()
														.getSize();
												return long1.compareTo(long2);
											}
										} catch (FileSystemException e) {
											throw new RuntimeException(
													"unable to sort the fields ",
													e);
										} finally {
											long1 = null;
											long2 = null;
										}

									}
								});
						break;
					}
					case MODIFIED_TIME: {
						Arrays.sort(sortedObjects,
								new Comparator<FileObject>() {

									/*
									 * (non-Javadoc)
									 * 
									 * @see
									 * java.util.Comparator#compare(java.lang
									 * .Object, java.lang.Object)
									 */
									@Override
									public int compare(FileObject o1,
											FileObject o2) {
										Long long1 = null;
										Long long2 = null;
										try {
											long1 = o1.getContent()
													.getLastModifiedTime();
											long2 = o2.getContent()
													.getLastModifiedTime();
											return long1.compareTo(long2);
										} catch (FileSystemException e) {
											throw new RuntimeException(
													"unable to sort the fields ",
													e);
										} finally {
											long1 = null;
											long2 = null;
										}
									}
								});
						break;

					}
					}

					if (sortOrder == SortOrder.DESCENDING) {
						Collections.reverse(Arrays.asList(sortedObjects));
					}
				}
			}

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("FileObjectSorter.sortAndGetChildren() completed");
			}
		}
		return sortedObjects;

	}
}
