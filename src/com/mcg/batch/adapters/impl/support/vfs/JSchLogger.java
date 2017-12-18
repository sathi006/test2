package com.mcg.batch.adapters.impl.support.vfs;

import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Logger;

public class JSchLogger implements Logger {

    public static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JSchLogger.class);

    @Override
    public boolean isEnabled(int level) {
	// TODO Auto-generated method stub
	if (level == DEBUG) {
	    if (!LOGGER.isDebugEnabled()) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public void log(int level, String message) {
	// TODO Auto-generated method stub
	if (level == Logger.DEBUG) {
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug(message);
	    }
	} else if (level == Logger.ERROR || level == Logger.FATAL) {
	    LOGGER.error(message);
	} else if (level == Logger.WARN) {
	    LOGGER.warn(message);
	} else {
	    LOGGER.info(message);
	}
    }

}
