package org.sustudio.concise.app;

import java.io.File;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * 雙擊檔案圖示時，要讓 Concise 能開啟該檔案
 * 
 * @author Kuan-ming Su
 *
 */
public class OpenDocumentEventProcessor implements Listener {

	private Launcher launcher;
	private String fileToOpen = new String();
	
	public OpenDocumentEventProcessor(Launcher launcher) {
		this.launcher = launcher;
	}
	
	@Override
	public void handleEvent(Event event) {
		if (event.text != null)
			fileToOpen = event.text;
	}
	
	public void openFile() {
		if (!fileToOpen.isEmpty() &&
			new File(fileToOpen).exists()) 
		{
			Concise.openApp(new File(fileToOpen));
			if (launcher != null && !launcher.isDisposed()) {
				launcher.dispose();
			}
		}
		fileToOpen = new String();
	}

}
