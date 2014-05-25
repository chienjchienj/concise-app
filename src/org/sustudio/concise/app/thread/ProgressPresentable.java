package org.sustudio.concise.app.thread;

public interface ProgressPresentable {

	/**
	 * Returns a float number between 0 and 1.
	 * @return
	 */
	public float getProgress();
	
	public String getMessage();
	
}
