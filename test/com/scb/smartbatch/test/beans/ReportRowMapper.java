/**
 * 
 */
package com.mcg.batch.test.beans;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class ReportRowMapper implements RowMapper<Report> {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ReportRowMapper.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet,
	 * int)
	 */
	@Override
	public Report mapRow(ResultSet rs, int rowNum) throws SQLException {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ReportRowMapper.mapRow() started");
		}
		Report report = new Report();

		try {

			report.setEpoch(rs.getString(1));
			report.setImpressions(rs.getString(2));
			report.setClicks(rs.getString(3));
			report.setEarning(rs.getString(4));
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("returning report " + report);
			}
			return report;

		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("ReportRowMapper.mapRow() completed");
			}
		}
	}

}
