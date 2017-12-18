package com.mcg.batch.utils;

import static com.mcg.batch.utils.StringHelper.SEMI_COLON;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesConfiguration {

    public static final Properties PROPERTIES = new Properties();

    /** Constant LOGGER. */
    public static final Logger LOGGER = LoggerFactory
	    .getLogger(PropertiesConfiguration.class);

    /** Constant IS_HOST_PART. */
    public static final String IS_HOST_PART = ".is.host";

    /** Constant IS_PORT_PART. */
    public static final String IS_PORT_PART = ".is.port";

    /** Constant IS_USERNAME_PART. */
    public static final String IS_USERNAME_PART = ".is.username";

    /** Constant IS_PASSWORD_PART. */
    public static final String IS_PASSWORD_PART = ".is.password";

    /** Constant DEFAULT_IS_PORT. */
    public static final String DEFAULT_IS_PORT = "5555";

    public static final String SERVER_PROPERTIES_FILE_KEY = "watt.scb.custom.properties.file";

    /**
     * prevent external instantiation
     */
    private PropertiesConfiguration() {

    }

    static {
	try {
	init(false);
	} catch (Exception e) {
	    e.printStackTrace();
	    LOGGER.error("Exception while loading custom configuration : ", e);
	}
    }

    public static void reinit() throws Exception {
		init(true);
    }
    
    private static void init(boolean isReload) throws Exception {
	File file = null;
	FileInputStream fis = null;
	try {
	    String filesListString = System.getProperty(
		    SERVER_PROPERTIES_FILE_KEY, "server-details.properties");
	    if (isReload) {
		PROPERTIES.clear();
	    }
	    if (StringUtils.isNotBlank(filesListString)) {
		if (isReload) {
			PROPERTIES.clear();
		    }
		String[] filesList = filesListString.split(SEMI_COLON);
		for (int i = 0; i < filesList.length; i++) {
		    try {
			file = new File(filesList[i]);
			fis = new FileInputStream(file);
			PROPERTIES.load(fis);
			if (LOGGER.isTraceEnabled()) {
			    LOGGER.trace("Properties after reading " + filesList[i] + " : " + PROPERTIES.toString());
			}
		    } catch (IOException e) {
			e.printStackTrace();
			LOGGER.error(
				"Error while loading custom configuration from "
					+ filesList[i] + " : ", e);
		    } finally {
			IOHelper.close(fis);
			file = null;
			fis = null;
		    }
		}
	    } else {
		throw new Exception("No value specified for watt.scb.custom.properties.file");
	    }
	} finally {
	    fis = null;
	    file = null;
	}
    }
    
    public static ServerDetailsDTO getServerDetails(String aliasName) {

	ServerDetailsDTO Serverdetailsdto = new ServerDetailsDTO();
	Serverdetailsdto.setHost(PROPERTIES.getProperty(aliasName
		+ IS_HOST_PART));
	Serverdetailsdto.setPort(PROPERTIES.getProperty(aliasName
		+ IS_PORT_PART, DEFAULT_IS_PORT));
	Serverdetailsdto.setUsername(PROPERTIES.getProperty(aliasName
		+ IS_USERNAME_PART));
	Serverdetailsdto.setPassword(PROPERTIES.getProperty(aliasName
		+ IS_PASSWORD_PART));
	return Serverdetailsdto;
    }

    public static final String getProperty(final String propertyName) {
	return PROPERTIES.getProperty(propertyName);
    }

    public static final int getIntProperty(final String propertyName) {
	return Integer.parseInt(getProperty(propertyName));
    }

    public static final float getFloatProperty(final String propertyName) {
	return Float.parseFloat(getProperty(propertyName));
    }

    public static final long getLongProperty(final String propertyName) {
	return Long.parseLong(getProperty(propertyName));
    }

    public static final double getDoubleProperty(final String propertyName) {
	return Double.parseDouble(getProperty(propertyName));
    }

    public static final boolean getBooleanProperty(final String propertyName) {
	return Boolean.valueOf(getProperty(propertyName));
    }

    public static final String getProperty(final String propertyName,
	    final String defaultVal) {
	return PROPERTIES.getProperty(propertyName, defaultVal);
    }

    public static final int getIntProperty(final String propertyName,
	    final String defaultVal) {
	return Integer.parseInt(getProperty(propertyName, defaultVal));
    }

    public static final float getFloatProperty(final String propertyName,
	    final String defaultVal) {
	return Float.parseFloat(getProperty(propertyName, defaultVal));
    }

    public static final long getLongProperty(final String propertyName,
	    final String defaultVal) {
	return Long.parseLong(getProperty(propertyName, defaultVal));
    }

    public static final double getDoubleProperty(final String propertyName,
	    final String defaultVal) {
	return Double.parseDouble(getProperty(propertyName, defaultVal));
    }

    public static final boolean getBooleanProperty(final String propertyName,
	    final String defaultVal) {
	return Boolean.valueOf(getProperty(propertyName, defaultVal));
    }
    
    public static final String getPropertiesString() {
	return PROPERTIES.toString();
    }

}
