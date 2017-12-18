/**
 * 
 */
package com.mcg.batch.utils;

import static com.mcg.batch.utils.IOHelper.close;
import static com.mcg.batch.utils.StringHelper.NEW_LINE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class OsCommand {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(OsCommand.class);

	private List<String> parameters = new ArrayList<String>();
	private String command = null;

	/**
	 * @param command
	 */
	public OsCommand(final String command) {
		super();
		this.command = command;
	}

	public void addParameters(final String... parameters) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Command.addParameters() started");
		}
		try {
			if (parameters != null && parameters.length > 0) {
				for (String parameter : parameters) {
					if (StringHelper.isNotEmpty(parameter)) {
						this.parameters.add(parameter);
					}
					parameter = null;
				}

			}
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Command.addParameters() completed");
			}
		}

	}

	public int executeAndGetStatus() throws InterruptedException, IOException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("OsCommand.executeAndGetStatus() started");
		}
		Process process = null;
		try {
			process = execute();
			process.getErrorStream().close();
			return process.waitFor();
		} finally {
			process = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("OsCommand.executeAndGetStatus() completed");
			}
		}
	}

	public String executeAndGetResult() throws IOException,
			InterruptedException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Command.executeAndGetResult() started");
		}
		Process process = null;
		InputStreamReader isri = null;
		InputStreamReader isre = null;
		BufferedReader bri = null;
		BufferedReader bre = null;
		StringBuilder result = null;
		String line = null;
		try {
			process = execute();
			result = new StringBuilder();
			isri = new InputStreamReader(process.getInputStream());
			isre = new InputStreamReader(process.getErrorStream());
			bri = new BufferedReader(isri);
			bre = new BufferedReader(isre);
			while ((line = bri.readLine()) != null) {
				System.out.println(line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				result.append(line);
				result.append(NEW_LINE);
			}
			close(bre);
			process.waitFor();

			return result.toString();
		} finally {
			close(bri, bre, isre, isri);
			bri = null;
			bre = null;
			isre = null;
			isri = null;
			line = null;
			result = null;
			process = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Command.executeAndGetResult() completed");
			}
		}
	}

	public Process execute() throws IOException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Command.execute() started");
		}

		try {

			return Runtime.getRuntime().exec(get());
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Command.execute() completed");
			}
		}

	}

	public String get() {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Command.get() started");
		}
		StringBuilder builder = new StringBuilder(command);
		Iterator<String> iterator = null;
		try {
			if (CollectionUtils.isNotEmpty(parameters)) {
				iterator = parameters.iterator();
				while (iterator.hasNext()) {
					builder.append(StringHelper.WHITE_SPACE);
					builder.append(iterator.next());
				}
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Returning the command " + builder.toString());
			}
			return builder.toString();
		} finally {
			builder = null;
			iterator = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Command.get() completed");
			}
		}

	}

	public static void main(String[] args) throws Exception {

		OsCommand command = new OsCommand("cmd /c");
		command.addParameters("dir");
		System.out.println(command.executeAndGetStatus());

	}
}
