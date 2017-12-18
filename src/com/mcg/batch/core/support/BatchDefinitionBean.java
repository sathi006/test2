package com.mcg.batch.core.support;

import static com.mcg.batch.core.BatchConfiguration.BATCH_DEPENDENT_ADAPTERS_LABEL;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class used to store batch Definition in cache as Serializable Object.
 *
 * @version 2.0
 * @since 1.0
 * @author Anandbabu Bolisetti
 */
public class BatchDefinitionBean implements Serializable {

	/**
	 * serialVersionUID.
	 */

	private static final long serialVersionUID = -6651059590933202468L;
	public static final String COUNTRY_KEY = "country";
	public static final String DOMAIN_KEY = "domain";
	public static final String BUSINESS_FUNCTION_KEY = "businessFunction";
	public static final String APPLICATION_KEY = "application";

	/**
	 * batchXml.
	 */
	private String batchXml = null;
	private String intermediateXML = null;
	private int version;
	private String batchName = null;
	private String[] parameterNames = null;

	/*
	 * private String projectName;
	 */

	/**
	 * params.
	 */
	private Map<String, Serializable> params;

	/**
	 * log.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchDefinitionBean.class);

	/**
	 * Constructor.
	 *
	 * @param btchXml
	 *            Byte
	 * @param params1
	 *            Map<String, String>
	 * @throws IOException
	 *             Exception
	 */

	/**
	 * @param batchXml
	 * @param intermediateXML
	 * @param version
	 * @param batchName
	 * @param params
	 */
	public BatchDefinitionBean(String batchXml, String intermediateXML,
			int version, String batchName, Map<String, Serializable> params,
			String[] parameterNames) {
		LOGGER.debug("BatchDefinitionBean Constructor Called");
		this.batchXml = batchXml;
		this.intermediateXML = intermediateXML;
		this.version = version;
		this.batchName = batchName;
		this.params = params;
		this.parameterNames = parameterNames != null ? parameterNames
				: new String[0];
	}

	/**
	 * Get params.
	 *
	 * @return the params
	 */
	public final Map<String, Serializable> getParams() {
		return params;
	}

	/**
	 * Set params.
	 *
	 * @param params1
	 *            Map<String, String>
	 */
	public final void setParams(final Map<String, Serializable> params1) {
		this.params = params1;
	}

	/**
	 * Get projectName.
	 *
	 * @return projectName String
	 */
	public final String getProjectName() {
		return (String) params.get("project");
	}

	/**
	 * Set projectName.
	 *
	 * @param projectName1
	 *            String
	 */
	/*
	 * public final void setProjectName(final String projectName1) {
	 * this.projectName = projectName1; }
	 *//**
	 * Get environment.
	 *
	 * @return String
	 */
	public final String getEnvironment() {
		return (String) params.get("environment");
	}

	/**
	 * Set environment.
	 *
	 * @param env
	 *            String
	 */
	/*
	 * public final void setEnvironment(final String env) { this.environment =
	 * env; }
	 *//**
	 * Get domain.
	 *
	 * @return String
	 */
	public final String getDomain() {
		return (String) params.get("domain");
	}

	/**
	 * Set domain.
	 *
	 * @param dom
	 *            String
	 */
	/*
	 * public final void setDomain(final String dom) { this.domain = dom; }
	 */

	/**
	 * Get batchXml.
	 *
	 * @return Byte
	 */
	public final byte[] getBatchXmlAsBytes() {
		return StringUtils.isNotEmpty(batchXml) ? batchXml.getBytes() : null;
	}

	/**
	 * Set batchXml.
	 *
	 * @param btchXml
	 *            Byte
	 */
	public final void setBatchXml(final String btchXml) {
		this.batchXml = btchXml;
	}

	/**
	 * @return the intermediateXML
	 */
	public byte[] getIntermediateXMLAsBytes() {
		return StringUtils.isNotEmpty(intermediateXML) ? intermediateXML.getBytes() : null;
	}

	/**
	 * @param intermediateXML
	 *            the intermediateXML to set
	 */
	public void setIntermediateXML(String intermediateXML) {
		this.intermediateXML = intermediateXML;
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @return the batchName
	 */
	public String getBatchName() {
		return batchName;
	}

	/**
	 * @param batchName
	 *            the batchName to set
	 */
	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	public void incrementVersion() {
		this.version += 1;
	}

	public boolean hasParamter(String parameterName) {
		return this.params.containsKey(parameterName);
	}

	public String[] getdependentAdapters() {
		return getParam(BATCH_DEPENDENT_ADAPTERS_LABEL, String[].class);
	}

	/**
	 * @return the parameterNames String[]
	 */
	public String[] getParameterNames() {
		return parameterNames;
	}

	@SuppressWarnings("unchecked")
	public <T extends Serializable> T getParam(final String paramName,
			Class<T> returnType) {
		return (T) params.get(paramName);
	}

}
