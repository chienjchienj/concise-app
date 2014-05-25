package org.sustudio.concise.app.query;

import org.sustudio.concise.app.gear.Gear;

/**
 * Default Concordance Query
 * 
 * @author Kuan-ming Su
 *
 */
public class DefaultConcQuery extends CAQuery {

	private static final long serialVersionUID = -8931726845829966408L;

	public DefaultConcQuery() {
		this("");
	}
	
	public DefaultConcQuery(final String word) {
		super(Gear.Concordancer);
		searchStr = word;
		leftSpanSize = 10;
		rightSpanSize = 10;
	}

}
