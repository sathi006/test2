/**
 * 
 */
package com.mcg.batch.test.processors;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@Component
public class SampleProcessor implements ItemProcessor<String, String> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	public String process(String input) throws Exception {
		System.out.println("Processor called");
		return "verified" + input;
	}

}
