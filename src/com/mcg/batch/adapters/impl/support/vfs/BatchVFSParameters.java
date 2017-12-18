/**
 * 
 */
package com.mcg.batch.adapters.impl.support.vfs;

import static com.mcg.batch.adapters.impl.support.vfs.MinFileAction.RETRY;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_IS_SINGLE_FILE;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_MIN_AGE_ACTION;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_MIN_FILE_AGE;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_PARAM_CHECK_SOURCE;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_PARAM_FILE_NAME;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_PARAM_FILTER_SORY_BY;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_PARAM_FILTER_SORY_ORDER;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_PARAM_MAX_FILE_COUNT;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_PARAM_MAX_FILE_SIZE;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_PARAM_TRANFER_TYPE;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_SRC_POST_SCRIPT;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_SRC_PRE_SCRIPT;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_TARGET_FILE_NAME;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_TARGET_RELATIVE_PATH;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_TGT_POST_SCRIPT;
import static com.mcg.batch.core.BatchConfiguration.BATCH_VFS_TGT_PRE_SCRIPT;
import static com.mcg.batch.core.BatchConfiguration.FTP_TRANSFER_MODE_KEY;
import static com.mcg.batch.core.BatchConfiguration.SFTP_COMPRESSION_KEY;
import static com.mcg.batch.utils.StringHelper.concat;
import static org.apache.commons.vfs2.FileName.SEPARATOR;
import static org.apache.commons.vfs2.FileName.SEPARATOR_CHAR;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.mcg.batch.utils.SortBy;
import com.mcg.batch.utils.SortOrder;
import com.mcg.batch.utils.StringHelper;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class BatchVFSParameters extends LinkedHashMap<String, String> {

	/**
	 * Auto generated serialVersionUID
	 */
	private static final long serialVersionUID = -7781958193802705588L;

	/**
	 * 
	 */
	public BatchVFSParameters(LinkedHashMap<String, String> map) {
		super(map);
	}

	/**
	 * @return the maxFileSize long
	 */
	public long getMaxFileSize() {
		return containsKey(BATCH_VFS_PARAM_MAX_FILE_SIZE) ? Long
				.parseLong(getValue(BATCH_VFS_PARAM_MAX_FILE_SIZE)) : -1L;
	}
	
	/**
	 * @return the maxFileCount int
	 */
	public int getMaxFileCount() {
		return containsKey(BATCH_VFS_PARAM_MAX_FILE_COUNT) ? Integer
				.parseInt(getValue(BATCH_VFS_PARAM_MAX_FILE_COUNT)) : -1;

	}

	
	
	/**
	 * @return the sortOrder SortOrder
	 */
	public SortOrder getSortOrder() {
		return containsKey(BATCH_VFS_PARAM_FILTER_SORY_ORDER) ? SortOrder
				.valueOf(getValue(BATCH_VFS_PARAM_FILTER_SORY_ORDER))
				: SortOrder.UNSORTED;
	}

	/**
	 * @return the checkSource boolean
	 */
	public boolean isCheckSource() {
		return containsKey(BATCH_VFS_PARAM_CHECK_SOURCE) ? Boolean
				.valueOf(getValue(BATCH_VFS_PARAM_CHECK_SOURCE)) : false;

	}

	/**
	 * @return the transferType TrasnferType
	 */
	public TransferType getTransferType() {
		return containsKey(BATCH_VFS_PARAM_TRANFER_TYPE) ? TransferType
				.valueOf(getValue(BATCH_VFS_PARAM_TRANFER_TYPE))
				: TransferType.CREATE;
	}

	public String getRelativeDirectory() {
		String definedFileName = getValue(BATCH_VFS_PARAM_FILE_NAME);

		if (definedFileName != null
				&& definedFileName.indexOf(SEPARATOR_CHAR) != -1) {
			String resolvedDirectory = definedFileName.substring(0,
					definedFileName.lastIndexOf(SEPARATOR_CHAR) + 1);
			return resolvedDirectory.startsWith(SEPARATOR) ? resolvedDirectory
					: concat(SEPARATOR_CHAR, resolvedDirectory);
		} else {
			return "";
		}
	}

	/**
	 * @return the fileName String
	 */
	public String getFileName() {
		String actualFileName = getValue(BATCH_VFS_PARAM_FILE_NAME);
		if (actualFileName != null
				&& actualFileName.indexOf(SEPARATOR_CHAR) != -1) {
			return actualFileName.substring(actualFileName
					.lastIndexOf(SEPARATOR_CHAR) + 1);
		} else {
			return actualFileName;
		}
	}

	public void setFileName(final String fileName) {
		put(BATCH_VFS_PARAM_FILE_NAME, fileName);
	}

	/**
	 * @return the compositeFileFilter CompositeFileFilter
	 */
	public CompositeFileFilter getCompositeFileFilter() {
	    	if (getMaxFileSize() > 0 && getMaxFileCount() > 0) {
			return new CompositeFileFilter(getMaxFileCount(), getMaxFileSize(),
					getFileName());
		} else if (getMaxFileSize() <= 0 && getMaxFileCount() > 0) {
			return new CompositeFileFilter(getMaxFileCount(), getFileName());
		} else if (getMaxFileSize() > 0 && getMaxFileCount() <= 0) {
			return new CompositeFileFilter(getMaxFileSize(), getFileName());
		} else {
			return new CompositeFileFilter(getFileName());
		}
	}

	/**
	 * @return the soryBy SortBy
	 */
	public SortBy getSortBy() {
		return containsKey(BATCH_VFS_PARAM_FILTER_SORY_BY) ? SortBy
				.valueOf(getValue(BATCH_VFS_PARAM_FILTER_SORY_BY))
				: SortBy.NAME;
	}

	public SftpCompressionMode getSftpCompressionMode() {
		return containsKey(SFTP_COMPRESSION_KEY) ? SftpCompressionMode
				.valueOf(getValue(SFTP_COMPRESSION_KEY))
				: SftpCompressionMode.NONE;
	}
	
	public FtpTransferMode getFtpTransferMode() {
		return containsKey(FTP_TRANSFER_MODE_KEY) ? FtpTransferMode
				.valueOf(getValue(FTP_TRANSFER_MODE_KEY))
				: FtpTransferMode.BINARY;
	}

	
	public String getSourcePreScript() {
		return getValue(BATCH_VFS_SRC_PRE_SCRIPT);
	}

	public String getSourcePostScript() {
		return getValue(BATCH_VFS_SRC_POST_SCRIPT);
	}

	public String getTargetPreScript() {
		return getValue(BATCH_VFS_TGT_PRE_SCRIPT);
	}

	public String getTargetPostScript() {
		return getValue(BATCH_VFS_TGT_POST_SCRIPT);
	}

	public void setParameters(Map<String, String> parameters) {
		this.putAll(parameters);
	}

	public long getMinFileAge() {
		String value = getValue(BATCH_VFS_MIN_FILE_AGE);
		if (StringUtils.isEmpty(value)) {
			return 0;
		} else {
			return Long.parseLong(value);
		}

	}

	public String getTargetRelativePath() {
	    if (containsKey(BATCH_VFS_TARGET_RELATIVE_PATH)) {
		String value = getValue(BATCH_VFS_TARGET_RELATIVE_PATH);
		if (StringUtils.isBlank(value)) {
		    return "";
		} else {
		   return value.startsWith(SEPARATOR) ? value
				: concat(SEPARATOR_CHAR, value);
		}
	    }
	    return "";
	}
	
	public boolean isSingleFile() {
	    if (containsKey(BATCH_VFS_IS_SINGLE_FILE)) {
		String value = getValue(BATCH_VFS_IS_SINGLE_FILE);
		if (StringUtils.isBlank(value)) {
		    return false;
		} else {
		   return Boolean.parseBoolean(value);
		}
	    }
	    return false;
	}
	
	public String getTargetFileName() {
	    if (containsKey(BATCH_VFS_TARGET_FILE_NAME)) {
		String value = getValue(BATCH_VFS_TARGET_FILE_NAME);
		if (StringUtils.isBlank(value)) {
		    return "";
		} else {
		   return value.startsWith(SEPARATOR) ? value
				: concat(SEPARATOR_CHAR, value);
		}
	    }
	    return "";
	}
	
	public MinFileAction getMinFileAction() {
		return containsKey(BATCH_VFS_MIN_AGE_ACTION) ? MinFileAction
				.valueOf(getValue(BATCH_VFS_MIN_AGE_ACTION)) : RETRY;
	}

	private String getValue(final String key) {
		String value = get(key);
		return value != null ? value.replaceAll(StringHelper.NEW_LINE, "")
				.trim() : null;
	}
	
	

}
