package com.mcg.batch.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Parent for all events in the batch system
 * 
 * @author sapu
 */
public class Event<H, B> implements Serializable {
	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -2688443323897105932L;

	private String eventName;
	private int eventType;

	protected Event(String eventName, int eventType) {
		this.eventName = eventName;
		this.eventType = eventType;
	}

	/** header. */
	private List<H> header = new ArrayList<H>();
	/** body. */
	private List<B> body = new ArrayList<B>();

	/**
	 * .
	 * 
	 * @return List
	 *
	 */
	public final List<H> getHeader() {
		return this.header;
	}

	/**
	 *
	 * @return body
	 */
	public final List<B> getBody() {
		return this.body;
	}

	/**
	 *
	 * @param head
	 *            header
	 */
	public final void setHeader(final List<H> head) {
		this.header = head;
	}

	/**
	 *
	 * @param bo
	 *            body
	 */
	public final void setBody(final List<B> bo) {
		this.body = bo;
	}

	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}

	/**
	 * @return the eventType
	 */
	public int getEventType() {
		return eventType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "{Event Type =" + getEventType()+
				"} Event Header={"+ getHeader()+ "} Event Body={"+ getBody()+
				"}";
	}
}
