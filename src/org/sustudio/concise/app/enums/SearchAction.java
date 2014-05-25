package org.sustudio.concise.app.enums;

public enum SearchAction {

	DEFAULT("Go"),
	
	WORD("Word"),
	
	//REGEX("Regex"),
	
	LIST("List");
	
	private final String label;
	
	SearchAction(final String label) {
		this.label = label;
	}
	
	public String label() {
		return label;
	}
	
}
