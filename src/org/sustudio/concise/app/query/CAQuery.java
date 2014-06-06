package org.sustudio.concise.app.query;

import java.io.Serializable;

import org.sustudio.concise.app.enums.SearchAction;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.core.CCPrefs;

/**
 * Concise App Query
 * 
 * @author Kuan-ming Su
 *
 */
public class CAQuery implements Serializable {

	private static final long serialVersionUID = 3166569980067221543L;
	
	/** Id of the gear that did the query */
	private final Gear gear;
	
	/** Search Word (text field on ConcoseToolBar) */
	public String searchStr = "";
	
	/** 
	 * Search Action
	 */
	public SearchAction searchAction;
	
	/** Left Span Size */
	public int leftSpanSize = CCPrefs.SPAN_SIZE_LEFT;
	
	/** Right Span Size */
	public int rightSpanSize = CCPrefs.SPAN_SIZE_RIGHT;
	
	/** If N-gram is On */
	public boolean ngram = false;
	
	
	
	/**
	 * Construct a concise query.
	 * @param gear	gear's id.
	 */
	public CAQuery(final Gear gear) {
		this.gear = gear;
		switch(gear) {
		case Concordancer:
		case ConcordancePlotter:
		case WordTrender:
		case ScatterPlotter:
		case Collocator:
		case CollocationalNetworker:
		case WordCluster:
		case DocumentViewer:
			searchAction = SearchAction.WORD;
			break;
		default:	
			searchAction = SearchAction.DEFAULT;	
			break;
		}
	}
	
	/**
	 * Create a new {@link CAQuery} from an existing query.
	 * @param query		concise query.
	 */
	public CAQuery(final CAQuery query) {
		gear = query.gear;
		searchStr = query.searchStr;
		searchAction = query.searchAction;
		leftSpanSize = query.leftSpanSize;
		rightSpanSize = query.rightSpanSize;
		ngram = query.ngram;
	}
	
	/**
	 * 
	 * @return
	 */
	public Gear getGear() {
		return gear;
	}
	
	public boolean equals(CAQuery query) {
		if (query == null ||
			!query.searchStr.equals(searchStr) ||
			query.searchAction != searchAction ||
			query.leftSpanSize != leftSpanSize ||
			query.rightSpanSize != rightSpanSize ||
			query.ngram != ngram 
			) {
			
			return false;
		}		
		return true;
	}
	
	public CAQuery copy() {
		return new CAQuery(this);
	}
	
	
	public String toString() {
		return getClass().getSimpleName() + 
			   " {" +
			   "gear=" + gear.name() + ", " +
			   "search=" + searchStr + ", " +
			   "action=" + searchAction.name() + ", " +
			   "L=" + leftSpanSize + ", " +
			   "R=" + rightSpanSize + 
			   "}";
	}
}
