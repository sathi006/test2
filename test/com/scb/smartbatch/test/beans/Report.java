/**
 * 
 */
package com.mcg.batch.test.beans;

public class Report {

	private String Epoch;
	private String Impressions;
	private String Clicks;
	private String Earning;

	/**
	 * @return the epoch String
	 */
	public String getEpoch() {
		return Epoch;
	}

	/**
	 * @param epoch
	 *            String
	 */
	public void setEpoch(String epoch) {
		Epoch = epoch;
	}

	public String getImpressions() {
		return Impressions;
	}

	public void setImpressions(String impressions) {
		Impressions = impressions;
	}

	public String getClicks() {
		return Clicks;
	}

	public void setClicks(String clicks) {
		Clicks = clicks;
	}

	public String getEarning() {
		return Earning;
	}

	public void setEarning(String earning) {
		Earning = earning;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append("epoch=");
		builder.append(Epoch);
		builder.append(",Impressions=");
		builder.append(Impressions);
		builder.append(",Clicks=");
		builder.append(Clicks);
		builder.append(",Earning=");
		builder.append(Earning);
		builder.append("}");

		return builder.toString();
	}
}