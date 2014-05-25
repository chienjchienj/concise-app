package org.sustudio.concise.app.enums;

import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.core.collocation.Collocate;

public enum EffectSizeMeasurement {

	/** 
	 * MI score
	 * <p>See {@link Collocate#getMI()} for detail. 
	 */
	MI("MI"),
	
	/** 
	 * MI3 score
	 * <p>See {@link Collocate#getMI3()} for detail. 
	 */
	MI3("MI3"),
	
	/** 
	 * Dice coefficient
	 * <p>See {@link Collocate#getDice()} for detail. 
	 */
	Dice("Dice"),
	
	/** 
	 * Odds ratio
	 * <p>See {@link Collocate#getOddsRatio()} for detail. 
	 */
	OddsRatio("Odds Ratio");
	
	
	private final String label;
	
	EffectSizeMeasurement(final String label) {
		this.label = label;
	}
	
	public String label() {
		return label;
	}
	
	public double valueOf(Collocate coll) {
		switch (this) {
		case MI:		return coll.getMI();
		case Dice:		return coll.getDice();
		case MI3:		return coll.getMI3();
		case OddsRatio:	return coll.getOddsRatio();
		default:		return 0d;
		}
	}
	
	public DBColumn dbColumn() {
		switch (this) {
		case MI:		return DBColumn.MI;
		case Dice:		return DBColumn.Dice;
		case MI3:		return DBColumn.MI3;
		case OddsRatio:	return DBColumn.OddsRatio;
		default:		return null;
		}
	}
}
