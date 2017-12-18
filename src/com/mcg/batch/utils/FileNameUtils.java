package com.mcg.batch.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcg.batch.runtime.impl.batch.utils.ParameterHelper;

public class FileNameUtils {
	
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileNameUtils.class);

	
	private FileNameUtils()
	{
		
	}

	public static final String retrieveFileName(String filePattern)
	{
		try
		{
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("FileNameUtils.getFileName() started");
		}
		
		Path path = Paths.get(filePattern);
		
		return path.getFileName().toString();
		}
		
		finally{
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("FileNameUtils.getFileName() completed");
		}
		}
	}
	

}
