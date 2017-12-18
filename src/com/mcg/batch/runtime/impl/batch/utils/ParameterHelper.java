/**
 * 
 */
package com.mcg.batch.runtime.impl.batch.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class ParameterHelper {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ParameterHelper.class);

	/**
	 * prevent external instantiation
	 */
	private ParameterHelper() {
	}

	public static final String currentDateFile(final String relativeDirectory,
			String datePattern, final String filePattern) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ParameterHelper.currentDateFile() started");
		}
		StringBuilder pathBuilder = null;
		DateFormat formatter = null;
		try {
			pathBuilder = new StringBuilder();
			pathBuilder.append(relativeDirectory);
			pathBuilder.append(FileName.SEPARATOR_CHAR);
			formatter = new SimpleDateFormat(datePattern);
			pathBuilder.append(formatter.format(new Date()));
			pathBuilder.append(FileName.SEPARATOR_CHAR);
			pathBuilder.append(filePattern);

			return pathBuilder.toString();
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("ParameterHelper.currentDateFile() completed");
			}
		}
	}

	public static final String currentDateDir(String datePattern) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ParameterHelper.currentDateFile() started");
		}

		datePattern = validateYearFormat(datePattern);
		DateFormat formatter = null;
		try {
			formatter = new SimpleDateFormat(datePattern);

			return formatter.format(new Date());
		} finally {
			formatter = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("ParameterHelper.currentDateFile() completed");
			}
		}
	}

	public static final String customDateDir(final String datePattern,
			final int daysAdded, final int monthsAdded, final int yearsAhead) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ParameterHelper.customDateDir() started");
		}
		return customDateDir(datePattern, null, daysAdded, monthsAdded,
				yearsAhead);
	}

	public static final String customDateDir(String datePattern,
			final String timeZone, final int daysAdded, final int monthsAdded,
			final int yearsAhead) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ParameterHelper.customDateDir() started");
		}
		DateFormat formatter = null;
		Calendar cal = Calendar.getInstance();
		try {
			datePattern = validateYearFormat(datePattern);
			cal.set(Calendar.DATE, cal.get(Calendar.DATE) + daysAdded);
			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + monthsAdded);
			cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + yearsAhead);
			formatter = new SimpleDateFormat(datePattern);
			if (timeZone != null && !timeZone.isEmpty()) {
				// cal.setTimeZone(TimeZone.getTimeZone(timeZone));
				return formatter.format(convertSystemTimeTODate(
						new Date(cal.getTimeInMillis()), timeZone));
			} else {
				return formatter.format(new Date(cal.getTimeInMillis()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			formatter = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("ParameterHelper.customDateDir() completed");
			}
		}
		return "";
	}

	public static final String currentHourFile(final String hourPattern,
			int hoursAdded) {

		DateFormat formatter = null;
		Calendar cal = Calendar.getInstance();
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("ParameterHelper.currentHourFile() started");
			}
			cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY)
					+ hoursAdded);

			formatter = new SimpleDateFormat(hourPattern);

			return formatter.format(new Date(cal.getTimeInMillis()));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			formatter = null;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("ParameterHelper.currentHourFile() completed");
			}
		}
		return "";
	}

	private static Date convertSystemTimeTODate(final Date dateTime,
			final String userTimeZone) throws Exception {
		DateFormat sdfToconvert = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat sdfTarget = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdfToconvert.setTimeZone(TimeZone.getTimeZone(userTimeZone));
		String stringDate = sdfToconvert.format(dateTime);
		String formattedDate = stringDate.concat(TimeZone.getDefault().getID());

		return sdfTarget.parse(formattedDate);
	}

	public static String validateYearFormat(String datePattern) {
		String regex = "Y(?=([^\"']*[\"'][^\"']*[\"'])*[^\"']*$)";
		datePattern = datePattern.replaceAll(regex, "y");
		return datePattern;
	}

	public static void main(String[] args) {
		// System.out.println(currentDateDir("'\\prd\\smartbatch\\debtlayer\\b3idl\\outbound\'dd'_'MMM'_'YYYY''"));
		// ("'GLEL_POS_GBL_ACCOUNTING_PL_'ddMMYYYY'.TXT.gz'")
		//System.out.println(currentHourFile("'RCWSRERE_0'HH'.txt'", -7));
		// System.out.println(currentDateDir(("'/test/abc/'YYYY/MM/dd'/'")));
		// System.out.println(currentDateDir("'MYFPX_'YY'_'MM'_'dd'_'HH':'mm'.dat'"));
		// System.out.println(currentDateDir("'dd_MM_YYYY"));
		// System.out.println(customHourFile("/home/batchadmin", "batchtest",
		// "logs", 4));
		// System.out.println(customDateDir("'asdsasd/asdaYYYYsdsrc-'dd-MM-YY-HH-mm'.txt'","GMT+10:00",-1,0,0));
		// System.out.println("ParameterHelper.main()" +
		// currentDateDir("'EDM_*_'YYYYMMdd'_MTH.txt'"));
		// Pattern.compile("#{T(com.mcg.batch.runtime.impl.batch.utils.ParameterHelper).currentDateDir('src-dd-MM.txt','0','1','0')}");
		 //Pattern.compile("#{T(com.mcg.batch.runtime.impl.batch.utils.ParameterHelper).customHourDir("/home/batchadmin/batchtest/RCW0'HH'_log",4)}");
		 //Pattern.compile("#{T(com.mcg.batch.runtime.impl.batch.utils.ParameterHelper).currentDateDir("/test/abc/YYYY/MM/dd/")}");
	}
}
