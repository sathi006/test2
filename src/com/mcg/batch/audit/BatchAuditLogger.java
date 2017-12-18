/**
 * 
 */
package com.mcg.batch.audit;

import org.springframework.batch.core.JobExecution;

import com.mcg.batch.events.Event;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public interface BatchAuditLogger {
	/**
	 * @param event
	 *            event
	 */
	public abstract void fatal(Event<?, ?> event);

	/**
	 * @param event
	 *            event
	 */
	public abstract void info(Event<?, ?> event);
	
	public abstract void info(Event<?, ?> event, JobExecution jobExecution);
}
