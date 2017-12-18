package com.mcg.batch.runtime.impl.item.readers.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.LineCallbackHandler;

public class SkippedLineCallbackHandler implements LineCallbackHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger(SkippedLineCallbackHandler.class);

    @Override
    public void handleLine(String arg0) {
	// TODO Auto-generated method stub
	LOGGER.info("A line has been skipped by Flat File Reader");
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Line Skipped is : " + arg0);
	}
    }

}
