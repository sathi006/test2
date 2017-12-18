package com.mcg.batch.cache.impl;

import static com.mcg.batch.core.BatchConfiguration.CACHE_MASTER_FILE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import com.webmethods.jms.log.Log;

/**
 * This class acts as listener for receiving messages from Redis Sentinel
 * regarding the Master Node changes. As part of this project we subscribe for
 * Master Switch Messages from Sentinel. OnMessage method is automatically fired
 * upon message from Sentinel.
 *
 */

public class SentinelMessageHandler implements MessageListener {

	/**
	 * log.
	 */
	private static Logger log = LoggerFactory
			.getLogger(SentinelMessageHandler.class);

	/**
	 * MASTER_HOST_INDEX.
	 */
	private static final int MASTER_HOST_INDEX = 3;

	/**
	 * MASTER_PORT_INDEX.
	 */
	private static final int MASTER_PORT_INDEX = 4;

	/**
	 * This method is fired automatically when there is a message from Redis
	 * Sentinel.
	 * 
	 * @param arg0
	 *            Message
	 * @param arg1
	 *            Byte
	 */
	@Override
	public final void onMessage(final Message arg0, final byte[] arg1) {
		FileInputStream filein = null;
		FileOutputStream fileout = null;
		
		String message = arg0.toString();
		Log.info(message);
		String[] parts = message.split("\\s");
		String cacheMasterHost = parts[MASTER_HOST_INDEX];
		String cacheMasterPort = parts[MASTER_PORT_INDEX];
		log.info("Master Changed :: New Host:[" + cacheMasterHost + "]:"
				+ " New Port:[" + cacheMasterPort + "]");
		Properties prop = new Properties();
		try {
			filein = new FileInputStream(CACHE_MASTER_FILE);
			fileout = new FileOutputStream(CACHE_MASTER_FILE);
			prop.load(filein);
			prop.setProperty("cache.master.host", cacheMasterHost);
			prop.setProperty("cache.master.port", cacheMasterPort);
			prop.store(fileout, null);
		} catch (FileNotFoundException e) {
			log.error("Exception while accessing cache master file. ", e);
		} catch (IOException e) {
			log.error("Exception while accessing cache master file. ", e);
		} finally {
			try {
				filein.close();
				fileout.close();
			} catch (Exception e) {
				log.error("Exception while closing cache master file. ", e);
			}
		}
	}

}
