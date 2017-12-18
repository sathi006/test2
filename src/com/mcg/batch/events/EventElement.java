package com.mcg.batch.events;

import java.io.Serializable;

/**
 * @author jaja POJO used as granular element in creation of Events.
 */
public class EventElement implements Serializable {
	/**
     *
     */
	private static final long serialVersionUID = 1L;
	/** key. */
	private String key;
	/** value. */
	private Object value;

	/**
	 * @param k
	 *            key
	 * @param v
	 *            value
	 */
	public EventElement(final String k, final Object v) {
		this.key = k;
		this.value = v;
	}

	/**
	 * @return key
	 */
	public final String getKey() {
		return key;
	}

	/**
	 * @param k
	 *            key
	 */
	public final void setKey(final String k) {
		this.key = k;
	}

	/**
	 * @return value
	 */
	public final Object getValue() {
		return value;
	}

	/**
	 * @param v
	 *            value
	 */
	public final void setValue(final Object v) {
		this.value = v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + key + "," + value + "}";
	}

}
