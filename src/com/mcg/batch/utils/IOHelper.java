/**
 * 
 */
package com.mcg.batch.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility class for creating
 * 
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class IOHelper {

	public static final int KB = 1024;

	public static final int MB = 1024 * KB;

	public static final int GB = 1025 * MB;

	public static final long TB = 1024 * GB;

	public static final int DEFAULT_BUFFER_SIZE = 4 * KB;

	public static final String DEFAULT_BUFFER_SIZE_STR = "4096";

	public static final String READ_WRITE_MODE = "rw";
	public static final String READ_MODE = "r";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(IOHelper.class);

	/**
	 * prevent external instantiation.
	 */
	private IOHelper() {
	}

	/**
	 * Closes the {@link Closeable} silently.
	 * 
	 * @param closeable
	 */
	public static final void close(final Closeable closeable) {

		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {
			LOGGER.error("Exception closing the resource", e);
		}
	}

	/**
	 * Closes the array {@link Closeable} silently.
	 * 
	 * @param closeables
	 */
	public static final void close(final Closeable... closeables) {
		if (closeables != null) {
			for (Closeable closeable : closeables) {
				try {
					close(closeable);
				} finally {
					closeable = null;
				}
			}
		}
	}

	/**
	 * Closes the {@link FileObject} silently
	 * 
	 * @param fileObject
	 */
	public static final void close(final FileObject fileObject) {

		if (fileObject != null) {
			try {
				fileObject.close();
			} catch (FileSystemException e) {
				LOGGER.error(
						"Unable to close the fileObject will continue withouth blocking it as an execption... ",
						e);
			}
		}
	}

	/**
	 * Closes the array {@link FileObject} silently
	 * 
	 * @param fileObjects
	 */

	public static final void close(final FileObject... fileObjects) {
		if (fileObjects != null) {
			for (FileObject fileObject : fileObjects) {
				try {
					close(fileObject);
				} finally {
					fileObject = null;
				}
			}
		}
	}

	/**
	 * Reads the input stream for entire length specified and returns the
	 * bytes[] read<br>
	 * This method does not close the inputstream
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static byte[] getBytes(final InputStream inputStream)
			throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			transfer(inputStream, byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		} finally {
			byteArrayOutputStream = null;
		}

	}

	/**
	 * Reads the input stream for the length specified and returns the bytes[]
	 * read
	 * 
	 * @param inputStream
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static byte[] getBytes(final InputStream inputStream,
			final int length) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			transfer(inputStream, byteArrayOutputStream, length);
			return byteArrayOutputStream.toByteArray();

		} finally {
			byteArrayOutputStream = null;
		}

	}

	/**
	 * Transfer from InputStream to OutputStream for the specified length<br>
	 * This method does not close the streams. The calling method should take
	 * care of closing the resources.
	 * 
	 * @param inputStream
	 * @param outputStream
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static long transfer(final InputStream inputStream,
			final OutputStream outputStream, final int length)
			throws IOException {
		long transferedBytes = 0;
		int remainingBytes = length;
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

		try {
			for (int i = 0; (i != -1);) {
				if (remainingBytes > DEFAULT_BUFFER_SIZE) {
					i = inputStream.read(buffer);
					transferedBytes += i;
					outputStream.write(buffer, 0, i);
				} else {
					int readBytes = inputStream.read(buffer, 0, length);
					transferedBytes += readBytes;
					outputStream.write(buffer, 0, readBytes);
					break;
				}
				remainingBytes -= i;

			}

		} finally {
			buffer = null;
		}

		return transferedBytes;
	}

	/**
	 * Transfer from InputStream to OutputStream for the entire length of the
	 * input stream<br
	 * This method does not close the streams. The calling method should take
	 * care of closing the resources.
	 * 
	 * @param inputStream
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */

	public static long transfer(final InputStream inputStream,
			final OutputStream outputStream) throws IOException {
		long transferedBytes = 0;
		int i = -1;

		int bufferSize = Integer.parseInt(System.getProperty(
				"smartbatch.stream.transfer.buffer.size",
				DEFAULT_BUFFER_SIZE_STR));
		byte[] bytes = new byte[bufferSize];
		try {

			while ((i = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, i);
				transferedBytes += i;
			}
			outputStream.flush();
			return transferedBytes;
		} finally {
			bytes = null;
		}

	}

	/*public static long transferWithEncoding(final InputStream inputStream,
			final OutputStream outputStream, String sourceEncoding,
			String targetEncoding) throws IOException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("The Target Transfer mode is ---------"
					+ targetEncoding);
		}
		long transferedBytes = 0;
		int i = -1;
		int bufferSize = Integer.parseInt(System.getProperty(
				"smartbatch.stream.transfer.buffer.size",
				DEFAULT_BUFFER_SIZE_STR));
		byte[] getBytes = new byte[bufferSize];
		String record = "";
		StringBuffer content = new StringBuffer();
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		if (sourceEncoding.isEmpty()) {
			bufferedReader = new BufferedReader(new InputStreamReader(
					inputStream));
		} else {
			bufferedReader = new BufferedReader(new InputStreamReader(
					inputStream, Charset.forName(sourceEncoding)));
		}
		if (targetEncoding.isEmpty()) {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					outputStream));
		} else {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					outputStream, Charset.forName(targetEncoding)));
		}

		try {
			while ((record = bufferedReader.readLine()) != null) {
                content.append(record+"\n");
                content.append(System.lineSeparator());
                
				getBytes = record.getBytes();
				i = getBytes.length;
				bufferedWriter.write(record + "\n");
				//bufferedWriter.newLine();
				transferedBytes += i;

			}
			bufferedWriter.flush();
			return transferedBytes;

		} finally {
			getBytes = null;
		}

	}
*/
	/**
	 * Adds a file to the Zip output stream.<br>
	 * This method does not close the {@link ZipOutputStream}. The calling
	 * method should take care of closing the resources.
	 * 
	 * @param file
	 * @param zos
	 * @throws IOException
	 */
	public static void addToZipFile(File file, ZipOutputStream zos)
			throws IOException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("IOHelper.addToZipFile() started");
		}
		FileInputStream fis = null;
		ZipEntry zipEntry = null;
		try {
			fis = new FileInputStream(file);
			zipEntry = new ZipEntry(file.getName());
			zos.putNextEntry(zipEntry);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}
		} finally {
			zos.closeEntry();
			close(fis);
			zipEntry = null;
			fis = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("IOHelper.addToZipFile() completed");
			}
		}

	}

	/**
	 * Adds a file to the Zip output stream.<br>
	 * This method does not close the {@link ZipOutputStream}. The calling
	 * method should take care of closing the resources.
	 * 
	 * @param fileName
	 * @param zos
	 * @throws IOException
	 */
	public static void addToZipFile(final String fileName,
			final ZipOutputStream zos) throws IOException {
		addToZipFile(new File(fileName), zos);
	}

	/**
	 * Archives the directory and creates the zip file.<br>
	 * Post archive the source directory is not deleted.
	 * 
	 * @param directoryToZip
	 * @param zipFileName
	 * @throws IOException
	 */
	public static final void zipDirectory(final String directoryToZip,
			final String zipFileName) throws IOException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("IOHelper.zipDirectory() started");
		}
		File directoryToZipFile = new File(directoryToZip);
		File zipFile = new File(zipFileName);

		try {
			zipDirectory(directoryToZipFile, zipFile);
		} finally {
			directoryToZipFile = null;
			zipFile = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("IOHelper.zipDirectory() completed");
			}
		}
	}

	/**
	 * Archives the directory and creates the zip file.<br>
	 * Post archive the source directory is not deleted.
	 * 
	 * @param directoryToZip
	 * @param zipFile
	 * @throws IOException
	 */
	public static final void zipDirectory(final File directoryToZip,
			final File zipFile) throws IOException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("IOHelper.zipDirectory() started");
		}
		FileOutputStream fos = null;
		FileInputStream fis = null;
		ZipOutputStream zos = null;
		List<String> filePaths = null;
		ZipEntry zipEntry = null;
		try {
			filePaths = new ArrayList<String>();
			getAllFiles(directoryToZip, filePaths);
			fos = new FileOutputStream(zipFile);
			zos = new ZipOutputStream(fos);

			for (String filePath : filePaths) {
				zipEntry = new ZipEntry(filePath.substring(directoryToZip
						.getAbsolutePath().length() + 1, filePath.length()));
				zos.putNextEntry(zipEntry);
				fis = new FileInputStream(filePath);
				transfer(fis, zos);
				zos.closeEntry();
				close(fis);
				zipEntry = null;
				filePath = null;
			}
		} finally {
			close(fis, zos, fos);
			fis = null;
			zos = null;
			fos = null;
			filePaths = null;
			zipEntry = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("IOHelper.zipDirectory() completed");
			}
		}
	}

	/**
	 * Adds all children of the directory to the list.If the input {@link File}
	 * is not a directory then it adds the file absolute path.
	 * 
	 * @param directoryToScan
	 * @param listTouse
	 */
	public static final void getAllFiles(File directoryToScan,
			List<String> listTouse) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("IOHelper.getAllFiles() started");
		}
		File[] files = null;
		try {
			if (directoryToScan.isFile()) {
				listTouse.add(directoryToScan.getAbsolutePath());
				return;
			}
			files = directoryToScan.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					listTouse.add(file.getAbsolutePath());
				} else
					getAllFiles(file, listTouse);
				file = null;
			}
		} finally {
			files = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("IOHelper.getAllFiles() completed");
			}
		}
	}

	/**
	 * Delete the file or directory and its children recursively
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static final void deleteRecursive(final File file)
			throws IOException {
		if (file == null) {
			return;
		}
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File childFile = new File(file, temp);
					deleteRecursive(childFile);
					childFile = null;
				}
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			file.delete();
		}
	}

}
