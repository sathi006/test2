/**
 * 
 */
package com.mcg.batch.utils;

import static com.mcg.batch.utils.IOHelper.READ_MODE;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Nanda Gopalan
 * @version 1.0
 * @since 1.0
 * 
 */
public class StringHelper {

	public static final String EMPTY_STRING = "";
	public static final String WHITE_SPACE = " ";
	public static final String COMMA = ",";
	public static String NEW_LINE = "\n";
	public static final String COLON = ":";
	public static final String SEMI_COLON = ";";
	public static final String EQUALS = "=";
	public static final String NT_NEW_LINE = "\r\n";
	public static final String UX_NEW_LINE = "\n";
	public static final char DOUBLE_QUOTE_CHAR = '"';
	public static final char QUOTE_CHAR = '\'';
	public static final char EQUALS_CHAR = '=';
	public static final char AT_THE_RATE_CHAR = '@';
	public static final char COLON_CHAR = ':';
	public static final char SEMI_COLON_CHAR = ';';
	public static final char PERIOD_CHAR = '.';
	public static final char COMMA_CHAR = ',';
	public static final char HYPHEN_CHAR = '-';
	public static final char DEFAULT_ESCAPE_CHAR = '\\';
	public static final String HASH_CHAR = "#";
	public static final String NEVER = "Never";
	public static final String SCRIPT_PARAM_DELIMITER = "\\|";
	public static final String WILDCARD = "*";
	

	/**
	 * Prevent external instantiation.
	 */
	private StringHelper() {
	}

	static {
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		pw.println();
		NEW_LINE = writer.toString();
	}

	public static boolean isEmpty(String string) {
		return string != null && string.length() == 0;
	}

	public static boolean isNullOrEmpty(String string) {
		return string == null || string.length() == 0;
	}

	public static boolean isNotEmpty(String string) {
		return string != null && string.length() > 0;
	}

	public static boolean isEquals(String string1, String string2) {
		return string1.equals(string2);
	}

	public static boolean notEquals(String string1, String string2) {
		return !isEquals(string1, string2);
	}

	public static String concat(Object... objects) {
		StringBuilder builder = null;
		try {
			if (objects != null) {
				builder = new StringBuilder();
				for (Object object : objects) {
					builder.append(object);
					object = null;
				}
				return builder.toString();
			} else {
				return null;
			}
		} finally {
			builder = null;
		}
	}

	public static String concatNotNulls(Object... objects) {
		StringBuilder builder = null;
		try {
			if (objects != null) {
				builder = new StringBuilder();
				for (Object object : objects) {
					if (object != null) {
						builder.append(object);
					}
					object = null;
				}
				return builder.toString();
			} else {
				return null;
			}
		} finally {
			builder = null;
		}
	}

	public static final Map<String, String> delimitedStringsToMap(
			String delimitedString) {
		return delimitedStringsToMap(delimitedString, COMMA_CHAR);
	}

	public static final Map<String, String> delimitedStringsToMap(
			String delimitedString, char delimiter) {

		return stringListToMap(delimitedStringToArray(delimitedString,
				delimiter));

	}

	public static final Map<String, String> stringListToMap(String[] stringList) {
		Map<String, String> properties = new LinkedHashMap<String, String>();
		String[] entry = null;
		if (stringList != null) {
			for (int i = 0; i < stringList.length; i++) {
				entry = delimitedStringToArray(stringList[i], EQUALS_CHAR);
				if (entry != null && entry.length == 2) {
					properties.put(entry[0], entry[1]);
				}
			}
		}
		return properties;
	}

	public static final Properties delimitedStringsToProperty(
			String delimitedString) {
		return delimitedStringsToProperty(delimitedString, COMMA_CHAR);
	}

	public static final Properties delimitedStringsToProperty(
			String delimitedString, char delimiter) {

		return stringListToProperty(delimitedStringToArray(delimitedString,
				delimiter));

	}

	public static final Properties stringListToProperty(String[] stringList) {
		Properties properties = new Properties();
		String[] entry = null;
		if (stringList != null) {
			for (int i = 0; i < stringList.length; i++) {
				entry = delimitedStringToArray(stringList[i], EQUALS_CHAR);
				if (entry != null && entry.length == 2) {
					properties.put(entry[0], entry[1]);
				}
			}
		}

		return properties;
	}

	public static final String[] delimitedStringToArray(String delimitedString,
			char delimiterChar) {
		return delimitedStringToArray(delimitedString, delimiterChar,
				DEFAULT_ESCAPE_CHAR);
	}

	public static final String[] delimitedStringToArray(String delimitedString,
			char delimiterChar, char escapeChar) {
		String[] result = null;
		int index = 0;
		List<String> resultList = null;
		StringBuilder entry = new StringBuilder();
		if (isEmpty(delimitedString)) {
			result = new String[0];
		} else {
			resultList = new ArrayList<String>();
			boolean notFound = true;
			for (int i = 0; i < delimitedString.length(); i++) {
				if ((delimitedString.charAt(i) == delimiterChar && delimitedString
						.charAt(i - 1) != escapeChar)
						|| i == delimitedString.length() - 1) {

					entry.append(delimitedString.substring(
							index,
							(i == delimitedString.length() - 1) ? delimitedString
									.length() : i));
					for (int j = 0; j < entry.length(); j++) {
						if (entry.charAt(j) == escapeChar
								&& entry.charAt(j + 1) == delimiterChar) {
							entry.deleteCharAt(j);
						}
					}
					resultList.add(entry.toString());
					entry.delete(0, entry.length());
					index = i + 1;
					notFound = false;
				}
			}
			if (notFound) {
				resultList.add(delimitedString);
			}
			result = resultList.toArray(new String[resultList.size()]);
			resultList = null;
		}

		return result;

	}

	public static String delimit(String delimiter, Object... objects) {

		StringBuilder builder = null;
		try {
			if (objects != null && delimiter != null) {
				builder = new StringBuilder();

				for (int i = 0; i < objects.length; i++) {
					builder.append(objects[i]);
					if (i != objects.length - 1) {
						builder.append(delimiter);
					}

				}
				return builder.toString();
			} else {
				return null;
			}
		} finally {
			builder = null;
		}

	}

	public static String createCSV(Object... objects) {
		return delimit(COMMA, objects);
	}

	public static final String getFile(String file) {
		return getFile(new File(file));
	}

	public static final String getFile(File file) {
		StringBuilder builder = new StringBuilder();
		RandomAccessFile raf = null;
		String temp = null;
		try {
			raf = new RandomAccessFile(file, READ_MODE);
			while ((temp = raf.readLine()) != null) {
				builder.append(temp);

				// builder.append(NEW_LINE);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOHelper.close(raf);
		}
		return builder.toString();
	}
}
