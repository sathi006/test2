/**
 * 
 */
package com.mcg.batch.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class SmartBatchJobParametersConvertor extends
		DefaultJobParametersConverter {

	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmartBatchJobParametersConvertor.class);

	public final static ExpressionParser PARSER = new SpelExpressionParser();

	public static final String DATE_TYPE = "(date)";
	public static final String STRING_TYPE = "(string)";
	public static final String LONG_TYPE = "(long)";
	private static final String DOUBLE_TYPE = "(double)";
	private static final String SOURCE_PRAM_TYPE = "source-";
	private static final String TARGET_PARAM_TYPE = "target-";
	private static final char NON_IDENTIFYING_FLAG = '-';
	private static final char IDENTIFYING_FLAG = '+';
	private static NumberFormat DEFAULT_NUMBER_FORMAT = NumberFormat
			.getInstance(Locale.US);

	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

	private NumberFormat numberFormat = DEFAULT_NUMBER_FORMAT;

	public JobParameters getJobParameters(final Properties props) {
		if ((props == null) || (props.isEmpty())) {
			return new JobParameters();
		}

		JobParametersBuilder propertiesBuilder = new JobParametersBuilder();

		for (Iterator<Entry<Object, Object>> it = props.entrySet().iterator(); it
				.hasNext();) {
			Map.Entry<?, ?> entry = it.next();
			StringBuilder keyBuilder = new StringBuilder(
					(String) entry.getKey());
			String value = (String) entry.getValue();

			boolean identifying = isIdentifyingKey(keyBuilder);
			String key = keyBuilder.toString();

			if (key.endsWith(DATE_TYPE)) {
				Date date;
				try {
					date = this.dateFormat.parse(value);
				} catch (ParseException ex) {
					String suffix = (this.dateFormat instanceof SimpleDateFormat) ? ", use "
							+ ((SimpleDateFormat) this.dateFormat).toPattern()
							: "";

					throw new IllegalArgumentException(
							"Date format is invalid: [" + value + "]" + suffix);
				}
				propertiesBuilder.addDate(
						StringUtils.replace(key, DATE_TYPE, ""), date,
						identifying);
			} else if (key.endsWith(LONG_TYPE)) {
				Long result;
				try {
					result = (Long) parseNumber(value);
				} catch (ClassCastException ex) {
					throw new IllegalArgumentException(
							"Number format is invalid for long value: ["
									+ value
									+ "], use a format with no decimal places");
				}

				propertiesBuilder.addLong(
						StringUtils.replace(key, LONG_TYPE, ""), result,
						identifying);
			} else if (key.endsWith(DOUBLE_TYPE)) {
				Double result = Double
						.valueOf(parseNumber(value).doubleValue());
				propertiesBuilder.addDouble(
						StringUtils.replace(key, DOUBLE_TYPE, ""), result,
						identifying);
			} else if (StringUtils.endsWithIgnoreCase(key, STRING_TYPE)) {
				propertiesBuilder.addString(
						StringUtils.replace(key, STRING_TYPE, ""), value,
						identifying);
			} else if (StringUtils.startsWithIgnoreCase(key, SOURCE_PRAM_TYPE)
					|| StringUtils.startsWithIgnoreCase(key, TARGET_PARAM_TYPE)) {
				key = key.replaceAll("-", "");
				if (isExpression(value)) {

					Expression exp = PARSER
							.parseExpression(removeExpressionConstants(value));
					value = exp.getValue(String.class);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("the expression is evaluated and the value is "
								+ value);
					}

				}
				propertiesBuilder.addString(key, value, identifying);
			} else {
				propertiesBuilder.addString(key, value, identifying);
			}
		}

		return propertiesBuilder.toJobParameters();
	}

	private boolean isIdentifyingKey(final StringBuilder keyBuilder) {
		boolean identifying = true;

		if (keyBuilder.charAt(0) == NON_IDENTIFYING_FLAG) {
			identifying = false;
			keyBuilder.deleteCharAt(0);
		} else if (keyBuilder.charAt(0) == IDENTIFYING_FLAG) {
			keyBuilder.deleteCharAt(0);
		}

		return identifying;
	}

	public void setNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	private Number parseNumber(String value) {
		String suffix;
		try {
			return this.numberFormat.parse(value);
		} catch (ParseException ex) {
			suffix = (this.numberFormat instanceof DecimalFormat) ? ", use "
					+ ((DecimalFormat) this.numberFormat).toPattern() : "";
		}
		throw new IllegalArgumentException("Number format is invalid: ["
				+ value + "], use " + suffix);
	}

	public static void main(String[] args) {
		// String delimitedString =
		// "abc=xyz,methodParam=#{T(com.mcg.batch.utils.SmartBatchJobParametersConvertor).simpleMethod('str1\\,str2')}";
		//
		// System.out.println(PropertiesConverter
		// .stringToProperties(delimitedString));
		String key = "part1-part2-part3";
		System.out.println(key.replaceAll("-", ""));
	}

	public static final String simpleMethod(String str1, String str2) {
		return str1 + str2;
	}

	private static final boolean isExpression(final String exprString) {
		return exprString.startsWith("#{") && exprString.endsWith("}");
	}

	private static final String removeExpressionConstants(
			final String expression) {
		StringBuilder builder = null;
		builder = new StringBuilder(expression);
		if (builder.charAt(0) == '#' && builder.charAt(1) == '{') {
			builder.delete(0, 1);
		}
		if (builder.charAt(1) == '}') {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}
}
