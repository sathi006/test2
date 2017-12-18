/**
 * 
 */
package com.mcg.batch.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import jcifs.smb.SmbAuthException;

import org.apache.commons.lang.StringUtils;

// TODO: Auto-generated Javadoc
/**
 * A utility to get the Stack trace a a String.
 *
 * @author Nanda Gopalan
 * @version 1.0
 * @since:1.0 
 */
public class StackUtils {

	/**
	 * prevent external instantiation.
	 */
	private StackUtils() {
	}

	/**
	 * Throwable to string.
	 *
	 * @param throwable the throwable
	 * @return the string
	 */
	public static String throwableToString(final Throwable throwable) {
		StringWriter writer = new StringWriter();
		PrintWriter pwriter = new PrintWriter(writer);
		throwable.printStackTrace(pwriter);
		return writer.toString();

	}
	
	/**
	 * Format exception.
	 *
	 * @param batchName the batch name
	 * @param adapterName the adapter name
	 * @param message the message
	 * @param throwable the throwable
	 * @return the string
	 */
	public static String formatException(String batchName, String adapterName, String message,Throwable throwable){
		
		return formatException(batchName, adapterName, message, throwable, false);
	}
	
	public static String formatException(String batchName, String adapterName, String message,Throwable throwable, boolean batchInitFailure) {
	    StringBuffer writer = new StringBuffer();
	    if (batchInitFailure) {
		writer.append("Exception during initialization of batch : ");
	    } else {
		writer.append("Exception during execution of batch : ");
	    }
	    if (StringUtils.isNotEmpty(batchName)) {
	     writer.append(batchName);
	    }
	    if (StringUtils.isNotEmpty(adapterName)) {
		writer.append(" : ");
		writer.append(adapterName);
	    }
	    if (StringUtils.isNotEmpty(message)) {
		writer.append(" : ");
		writer.append(message);
	    }
	    if (throwable != null) {
		writer.append(" : " + throwableToString(throwable));
	     }
	    return writer.toString();
	}
	
}