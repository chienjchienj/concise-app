package org.sustudio.concise.app.gear;

/**
 * Gear which is able to perform "Reveal In Finder" action
 * 
 * @author Kuan-ming Su
 *
 */
public interface IGearFileRevealable {

	/**
	 * Determine if there is any file to reveal
	 * @return true if reveal enabled.
	 */
	public boolean isRevealEnabled();
	
	/**
	 * Reveal a file in Finder.  This will be called from Pop-up context menu.
	 */
	public void revealFileInFinder();

	/**
	 * Open in Document Viewer (inside Concise).
	 */
	public void openFileInDocumentViewer();
	
}
