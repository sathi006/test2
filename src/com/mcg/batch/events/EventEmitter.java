package com.mcg.batch.events;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecutionListener;

/**
 *
 * implementation for all Listeners to listen on all events happening during
 * execution of Job. Such as before/after Job, before/after Step, before/after
 * read, before/after write, before/after process, on read/write/process error.
 *
 * Known implementations include BatchEventEmitter.
 *
 *
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 *
 */

@SuppressWarnings("rawtypes")
public interface  EventEmitter extends StepExecutionListener,
		JobExecutionListener, ItemReadListener, ItemProcessListener,
		ItemWriteListener {

}
