package org.sustudio.concise.app.enums;

import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.core.collocation.Collocate;

public enum SignificanceMeasurement {
	/**
	 * z-score measure
	 * <p>See {@link CCCollocate#getZscore()} for detail. 
	 */
	Zscore("z-score"),
	
	/** 
	 * t-score measure
	 * <p>See {@link CCCollocate#getTscore()} for detail.
	 */
	Tscore("t-score"),
	
	/** 
	 * simplified likelihood-ratio measure
	 * <p>See {@link CCCollocate#getSimpleLL()} for detail. 
	 */
	SimpleLL("Simple log-likelihood"),
	
	/** 
	 * chi-squared(corr) measure
	 * <p>See {@link CCCollocate#getChiSquaredCorr()} for detail. 
	 */
	ChiSquaredCorr("Chi-squared(Corr)"),
	
	/** 
	 * log-likelihood measure
	 * <p>See {@link CCCollocate#getLogLikelihood()} for detail. 
	 */ 
	LogLikelihood("Log-Likelihood");
	
	
	private final String label;
	
	SignificanceMeasurement(final String label) {
		this.label = label;
	}
	
	public String label() {
		return label;
	}
	
	public double valueOf(Collocate coll) {
		switch (this) {
		case ChiSquaredCorr:	return coll.getChiSquaredCorr();
		case LogLikelihood:		return coll.getLogLikelihood();
		case SimpleLL:			return coll.getSimpleLL();
		case Tscore:			return coll.getTscore();
		case Zscore:			return coll.getZscore();
		default:				return 0d;
		}
	}
	
	public DBColumn dbColumn() {
		switch (this) {
		case ChiSquaredCorr:	return DBColumn.ChiSquaredCorr;
		case LogLikelihood:		return DBColumn.LogLikelihood;
		case SimpleLL:			return DBColumn.SimpleLL;
		case Tscore:			return DBColumn.t_score;
		case Zscore:			return DBColumn.z_score;
		default:				return null;
		}
	}
}
