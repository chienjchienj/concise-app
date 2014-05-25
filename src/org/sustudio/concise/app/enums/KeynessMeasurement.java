package org.sustudio.concise.app.enums;

import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.core.keyword.Keyword;

public enum KeynessMeasurement {

	/** 
	 * Log-likelihood
	 * <p>See {@link Keyword#getLL()} for detail. 
	 */
	LogLikelihood("Log-likelihood"),
	
	/** 
	 * Yate's Chi-squared
	 * <p>See {@link Keyword#getChiSquare()} for detail. 
	 */
	ChiSquared("Yates' Chi-square");
	
	
	private final String label;
	
	KeynessMeasurement(final String label) {
		this.label = label;
	}
	
	public String label() {
		return label;
	}
	
	public DBColumn dbColumn() {
		switch (this) {
		case LogLikelihood:	return DBColumn.LogLikelihood;
		case ChiSquared:	return DBColumn.ChiSquaredCorr;
		default:			return null;
		}
	}
	
	public DBColumn pValueDBColumn() {
		switch (this) {
		case LogLikelihood:	return DBColumn.LogLikelihood_P;
		case ChiSquared:	return DBColumn.ChiSquaredCorr_P;
		default:			return null;
		}
	}
	
	public double valueOf(Keyword keyword) {
		switch (this) {
		case LogLikelihood:	return keyword.getLL();
		case ChiSquared:	return keyword.getChiSquare();
		default:			return 0d;
		}
	}
	
	public double pValueOf(Keyword keyword) throws Exception {
		switch (this) {
		case LogLikelihood:	return keyword.getLLPvalue();
		case ChiSquared:	return keyword.getChiSquarePvalue();
		default:			return 0d;
		}
	}
}
