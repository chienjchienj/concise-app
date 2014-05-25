package org.sustudio.concise.app.query;

import org.sustudio.concise.app.gear.Gear;

/**
 * Default Collocate Query (L4, R4)
 * 
 * @author Kuan-ming Su
 *
 */
public class DefaultCollocQuery extends CAQuery {

	private static final long serialVersionUID = -2311282528057173827L;

	public DefaultCollocQuery() {
		this("");
	}
	
	public DefaultCollocQuery(final String word) {
		super(Gear.Collocator);
		searchStr = word;
		leftSpanSize = 4;
		rightSpanSize = 4;
	}

}
