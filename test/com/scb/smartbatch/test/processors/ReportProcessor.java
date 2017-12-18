/**
 * 
 */
package com.mcg.batch.test.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcg.batch.test.beans.Report;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class ReportProcessor {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ReportProcessor.class);

	public static final Report transform(Report report) {
		Report report_1 = new Report();
		report_1.setClicks(report.getClicks());
		report_1.setEarning(report.getEarning());
		report_1.setEpoch(report.getEpoch());
		report_1.setImpressions(report.getImpressions());
		report_1.setClicks(report.getClicks() + "-transformed");
		return report_1;
	}

}
