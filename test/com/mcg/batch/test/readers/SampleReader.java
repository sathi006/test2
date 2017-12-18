/**
 * 
 */
package com.mcg.batch.test.readers;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@Component
public class SampleReader implements ItemReader<String> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemReader#read()
	 */

	private static long startExecutionId = 0;
	private static long endExecutionId = 101;

	private static ConcurrentHashMap<Long, AtomicLong> counters = new ConcurrentHashMap<Long, AtomicLong>();
	static {
		for (long i = startExecutionId; i < endExecutionId; i++) {
			counters.put(i, new AtomicLong());
		}
	}

	@Override
	public String read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		long lastValue = counters.get(ThreadContextUtils.getJobExecutionId())
				.getAndIncrement();
		if (lastValue < 10) {
			return "Content of Line Number " + lastValue;
		} else {
			return null;
		}

	}

}
