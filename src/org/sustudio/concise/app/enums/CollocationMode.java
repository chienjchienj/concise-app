package org.sustudio.concise.app.enums;

public enum CollocationMode {

	Surface ("Surface Collocation"),
	SentenceTextual ("Sentence Textual Collocation"),
	ParagraphTextual ("Paragraph Textual Collocation"),
	;
	
	private final String label;
	CollocationMode(String label) {
		this.label = label;
	}
	
	public String label() {
		return label;
	}

	public boolean isTextual() {
		return !equals(Surface);
	}

	public boolean isSurface() {
		return equals(Surface);
	}
}
