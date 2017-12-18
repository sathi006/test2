/**
 * 
 */
package com.mcg.batch.adapters.impl.support.rsync;

import static com.mcg.batch.core.BatchConfiguration.BATCH_RSYNC_PARAM_EXCLUDE_FILTER;
import static com.mcg.batch.core.BatchConfiguration.BATCH_RSYNC_PARAM_FILE_NAME;
import static com.mcg.batch.core.BatchConfiguration.BATCH_RSYNC_PARAM_INCLUDE_FILTER;
import static com.mcg.batch.core.BatchConfiguration.BATCH_RSYNC_PARAM_TRANFER_TYPE;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mcg.batch.utils.FileOperation;
import com.mcg.batch.utils.StringHelper;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class BatchRsyncParameters extends LinkedHashMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4210062340419873748L;

	public FileOperation getOperation() {

		return containsKey(BATCH_RSYNC_PARAM_TRANFER_TYPE) ? FileOperation
				.valueOf(get(BATCH_RSYNC_PARAM_TRANFER_TYPE))
				: FileOperation.COPY;

	}

	public String getFileName() {

		return getValue(BATCH_RSYNC_PARAM_FILE_NAME);

	}

	public String getExcludeFilter() {
		return getValue(BATCH_RSYNC_PARAM_EXCLUDE_FILTER);
	}

	public String getIncludeFilter() {
		return getValue(BATCH_RSYNC_PARAM_INCLUDE_FILTER);
	}

	public void setParameters(Map<String, String> parameters) {
		this.putAll(parameters);
	}

	private String getValue(final String key) {
		String value = get(key);
		return value != null ? value.replaceAll(StringHelper.NEW_LINE, "").trim()
				: null;
	}
}
