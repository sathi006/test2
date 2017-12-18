/**
 * 
 */
package com.mcg.batch.adapter;

import java.io.Serializable;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class AdapterDefinitionBean implements Serializable {

	/**
	 * Class serialVersionUID
	 */
	private static final long serialVersionUID = 3274276291296250229L;

	String adapterName;
	String adapterDefinition;
	String adapterUIDefinition;
	String type;
	int version;

	/**
	 * @param adapterName
	 * @param adapterDefinition
	 * @param adapterUIDefinition
	 */
	public AdapterDefinitionBean(final String adapterName, final String adapterDefinition,
			final String adapterUIDefinition,final String type) {
		super();
		this.adapterName = adapterName;
		this.adapterDefinition = adapterDefinition;
		this.adapterUIDefinition = adapterUIDefinition;
		this.type = type;
	}

	/**
	 * @return the type String
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the adapterName
	 */
	public String getAdapterName() {
		return adapterName;
	}

	/**
	 * @return the adapterDefinition
	 */
	public String getAdapterDefinition() {
		return adapterDefinition;
	}

	/**
	 * @param adapterDefinition
	 *            the adapterDefinition to set
	 */
	public void setAdapterDefinition(final String adapterDefinition) {
		this.adapterDefinition = adapterDefinition;
	}

	/**
	 * 
	 */
	public void incrementVersion() {
		this.version++;
	}

	/**
	 * @return the adapterUIDefinition String
	 */
	public String getAdapterUIDefinition() {
		return adapterUIDefinition;
	}

	/**
	 * @return the version int
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param adapterUIDefinition
	 *            String
	 */
	public void setAdapterUIDefinition(String adapterUIDefinition) {
		this.adapterUIDefinition = adapterUIDefinition;
	}

}
