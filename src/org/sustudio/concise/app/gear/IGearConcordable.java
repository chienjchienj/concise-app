package org.sustudio.concise.app.gear;

/**
 * Mark a gear that can perform concordance from its pop-up context menu.
 * 
 * @author Kuan-ming Su
 *
 */
public interface IGearConcordable {

	public boolean isConcordEnabled();
	
	public void showConcord();
	
}
